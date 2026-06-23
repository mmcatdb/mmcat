package cz.matfyz.server.adaptation;

import cz.matfyz.server.job.Job;
import cz.matfyz.server.querying.Query;
import cz.matfyz.server.querying.QueryRepository;
import cz.matfyz.server.utils.RequestContext;
import cz.matfyz.server.utils.Configuration.AdaptationProperties;
import cz.matfyz.server.utils.entity.Id;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
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

    @Autowired
    private QueryRepository queryRepository;

    @GetMapping("/schema-categories/{categoryId}/adaptation")
    public @Nullable Adaptation getAdaptationForCategory(@PathVariable Id categoryId) {
        return repository.tryFindByCategory(categoryId);
    }

    @PostMapping("/schema-categories/{categoryId}/adaptation")
    public Adaptation createAdaptationForCategory(@PathVariable Id categoryId) {
        return service.createForCategory(categoryId);
    }

    static class ProcessState {
        Date createdAt = new Date();
        volatile boolean isFinished = false;

        Process process;
        volatile @Nullable AdaptationSolution initialSolution;
        volatile @Nullable AdaptationResult lastResult;

        ProcessState(Process process) {
            this.process = process;
        }
    }

    final Map<Id, ProcessState> processes = new ConcurrentHashMap<>();

    @PostMapping("/adaptations/{adaptationId}/start")
    public AdaptationJob start(@PathVariable Id adaptationId) throws IOException {
        final Id sessionId = request.getSessionId();

        final var current = processes.get(sessionId);
        if (current != null && !current.isFinished)
            throw new RuntimeException("Session already active");

        final var adaptation = repository.find(adaptationId);
        if (adaptation == null)
            throw new RuntimeException("Adaptation not found for session");

        final var queries = queryRepository.findAllInCategory(adaptation.categoryId, null);
        final Process process = createAdaptationProcess(queries);
        final ProcessState processState = new ProcessState(process);

        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while (true) {
                    final String line = reader.readLine();
                    if (line == null)
                        break;

                    final var update = AdaptationProcessUpdate.fromJsonValue(line);

                    if (processState.initialSolution == null) {
                        if (update instanceof BestStateProcessUpdate bestState) {
                            final var initialResult = AdaptationResult.createInitial(bestState);
                            processState.initialSolution = initialResult.solutions().get(0);
                            processState.lastResult = initialResult;
                        }
                    }
                    else {
                        processState.lastResult = processState.lastResult.update(update);
                    }
                }

                processState.isFinished = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        processes.put(sessionId, processState);

        // On FE, the initial result has to be defined.
        while (processState.lastResult == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return AdaptationJob.create(processState);
    }

    private static final ObjectWriter mapWriter = new ObjectMapper().writerFor(Map.class);

    private Process createAdaptationProcess(List<Query> queries) throws IOException {
        final var weightsMap = new TreeMap<String, Double>();
        int index = 0;
        for (var query : queries) {
            // This is not generic but there is no other way right now.
            weightsMap.put("mcts-" + index, query.effectiveWeight());
            index++;
        }

        final var weights = mapWriter.writeValueAsString(weightsMap);

        System.out.println("Starting adaptation with weights: " + weights);

        final ProcessBuilder pb = new ProcessBuilder(
            ".venv/bin/python", "-m",
            adaptationProperties.scriptName(),
            "--iterations", adaptationProperties.iterations().toString(),
            "--latency-estimates", adaptationProperties.latencyEstimates(),
            "--storage-cost-weight", adaptationProperties.storageCostWeight().toString(),
            "--random-start",
            "--print-progress",
            "--silent",
            "--query-weights", weights
        );
        pb.directory(new File(adaptationProperties.pythonPath()));

        return pb.start();
    }

    @GetMapping("/adaptations/{adaptationId}/poll")
    public @Nullable AdaptationJob poll() {
        final Id sessionId = request.getSessionId();
        final ProcessState processState = processes.get(sessionId);
        if (processState == null)
            return null;

        return AdaptationJob.create(processState);
    }

    @PostMapping("/adaptations/{adaptationId}/stop")
    public void stop() {
        final Id sessionId = request.getSessionId();
        // This is more like a restart.
        final ProcessState processState = processes.remove(sessionId);
        if (processState == null || processState.process == null)
            throw new RuntimeException("Session already stopped");

        processState.process.destroy();
    }

    record AdaptationJob(
        AdaptationSolution initialSolution,
        AdaptationResult lastResult,
        Date createdAt,
        Job.State state
    ) {
        static AdaptationJob create(ProcessState state) {
            final var jobState = state.isFinished ? Job.State.Finished : Job.State.Running;
            return new AdaptationJob(state.initialSolution, state.lastResult, state.createdAt, jobState);
        }
    }

    private static final int MAX_BEST_SOLUTIONS = 3;

    record AdaptationResult(
        int iteration,
        int states,
        List<AdaptationSolution> solutions
    ) {
        static AdaptationResult createInitial(BestStateProcessUpdate bestState) {
            final var solution = new AdaptationSolution(bestState.states, bestState.state, bestState.cost);
            return new AdaptationResult(0, 0, List.of(solution));
        }

        AdaptationResult update(AdaptationProcessUpdate update) {
            if (update instanceof BestStateProcessUpdate bestState) {
                final var newSolutions = new ArrayList<>(solutions);
                final var newSolution = new AdaptationSolution(bestState.states, bestState.state, bestState.cost);
                newSolutions.addFirst(newSolution);

                if (newSolutions.size() > MAX_BEST_SOLUTIONS)
                    newSolutions.remove(newSolutions.size() - 1);

                return new AdaptationResult(bestState.iteration, bestState.states, newSolutions);
            }

            if (update instanceof ProgressProcessUpdate progress)
                return new AdaptationResult(progress.iteration, progress.states, solutions);

            throw new RuntimeException("Unknown update type");
        }
    }

    record AdaptationSolution(
        int id,
        Map<String, List<String>> objexes,
        double cost
    ) {}

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = BestStateProcessUpdate.class, name = "best-state"),
        @JsonSubTypes.Type(value = ProgressProcessUpdate.class, name = "progress"),
    })
    interface AdaptationProcessUpdate extends Serializable {

        static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(AdaptationProcessUpdate.class);

        static AdaptationProcessUpdate fromJsonValue(String jsonValue) throws JsonProcessingException {
            return jsonValueReader.readValue(jsonValue);
        }

    }

    record BestStateProcessUpdate(
        int iteration,
        int states,
        Map<String, List<String>> state,
        double cost
    ) implements AdaptationProcessUpdate {}

    record ProgressProcessUpdate(
        int iteration,
        int states
    ) implements AdaptationProcessUpdate {}

}
