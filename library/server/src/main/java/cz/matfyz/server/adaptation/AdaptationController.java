package cz.matfyz.server.adaptation;

import cz.matfyz.server.job.Job;
import cz.matfyz.server.utils.RequestContext;
import cz.matfyz.server.utils.Configuration.AdaptationProperties;
import cz.matfyz.server.utils.entity.Id;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdaptationController {

    @Autowired
    private RequestContext request;

    @Autowired
    private AdaptationRepository repository;

    @Autowired
    private AdaptationService service;

    @Autowired
    private AdaptationProperties adaptationProperties;

    @GetMapping("/schema-categories/{categoryId}/adaptation")
    public @Nullable Adaptation getAdaptationForCategory(@PathVariable Id categoryId) {
        return repository.tryFindByCategory(categoryId);
    }

    @PostMapping("/schema-categories/{categoryId}/adaptation")
    public Adaptation createAdaptationForCategory(@PathVariable Id categoryId) {
        return service.createForCategory(categoryId);
    }

    static class ProcessState {
        Process process;
        volatile @Nullable String initialJson;
        volatile @Nullable String lastJson;
        Date createdAt = new Date();
        volatile boolean isFinished = false;
    }

    final Map<Id, ProcessState> processes = new ConcurrentHashMap<>();

    @PostMapping("/adaptations/{adaptationId}/start")
    public AdaptationJob start() throws IOException {
        final Id sessionId = request.getSessionId();

        final var current = processes.get(sessionId);
        if (current != null && !current.isFinished)
            throw new RuntimeException("Session already active");

        final ProcessBuilder pb = new ProcessBuilder("python3", adaptationProperties.scriptPath());
        final Process process = pb.start();

        final ProcessState state = new ProcessState();
        state.process = process;

        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                boolean isFirstLine = true;

                while (true) {
                    final String line = reader.readLine();
                    if (line == null)
                        break;

                    if (isFirstLine) {
                        state.initialJson = line;
                        isFirstLine = false;
                    }

                    state.lastJson = line;
                }

                state.isFinished = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        processes.put(sessionId, state);

        while (state.lastJson == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return AdaptationJob.create(state);
    }

    record AdaptationJob(
        String initialJson,
        @Nullable String lastJson,
        Date createdAt,
        Job.State state
    ) {
        static AdaptationJob create(ProcessState state) {
            final var jobState = state.isFinished ? Job.State.Finished : Job.State.Running;
            return new AdaptationJob(state.initialJson, state.lastJson, state.createdAt, jobState);
        }
    }

    @GetMapping("/adaptations/{adaptationId}/poll")
    public @Nullable AdaptationJob poll() {
        final Id sessionId = request.getSessionId();
        final ProcessState state = processes.get(sessionId);
        if (state == null)
            return null;

        return AdaptationJob.create(state);
    }

    @PostMapping("/adaptations/{adaptationId}/stop")
    public void stop() {
        final Id sessionId = request.getSessionId();
        // This is more like a restart.
        final ProcessState state = processes.remove(sessionId);
        if (state == null || state.process == null)
            throw new RuntimeException("Session already stopped");

        state.process.destroy();
    }

}
