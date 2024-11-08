package cz.matfyz.server.controller;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.metadata.MetadataObject.Position;
import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.schemaconversion.utils.LayoutType;
import cz.matfyz.server.controller.ActionController.ActionPayloadDetail;
import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.job.JobData;
import cz.matfyz.server.entity.job.Session;
import cz.matfyz.server.entity.job.Job.State;
import cz.matfyz.server.entity.job.data.InferenceJobData;
import cz.matfyz.server.repository.ActionRepository;
import cz.matfyz.server.repository.JobRepository;
import cz.matfyz.server.repository.JobRepository.JobWithRun;
import cz.matfyz.server.service.JobExecutorService;
import cz.matfyz.server.service.JobService;
import cz.matfyz.server.service.WorkflowService;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class JobController {

    @Autowired
    private JobRepository repository;

    @Autowired
    private JobService service;

    @Autowired
    private JobExecutorService jobExecutorService;

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private ActionController actionController;

    @Autowired
    private WorkflowService workflowService;

    @GetMapping("/schema-categories/{categoryId}/jobs")
    public List<JobDetail> getAllJobsInCategory(@PathVariable Id categoryId, @CookieValue(name = "session", defaultValue = "") Id sessionId) {
        if (sessionId.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session ID is required.");

        final var jobs = repository.findAllInCategory(categoryId, sessionId);

        return jobs.stream().map(this::jobToJobDetail).toList();
    }

    @GetMapping("/jobs/{id}")
    public JobDetail getJob(@PathVariable Id id) {
        return jobToJobDetail(repository.find(id));
    }

    @PostMapping("/actions/{actionId}/jobs")
    public JobDetail createRun(@PathVariable Id actionId, @CookieValue(name = "session", defaultValue = "") Id sessionId) {
        if (sessionId.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session ID is required.");

        final var action = actionRepository.find(actionId);

        return jobToJobDetail(service.createUserRun(action, sessionId));
    }

    @PostMapping("/jobs/{id}/restart")
    public JobDetail createRestartedJob(@PathVariable Id id) {
        final var oldJob = repository.find(id);
        final var newJob = service.createRestartedJob(oldJob);

        // We have to update all workflows that depend on the job.
        workflowService.updateWorkflowsWithRestartedJob(oldJob.job(), newJob.job());

        return jobToJobDetail(newJob);
    }

    @PostMapping("/jobs/{id}/pause")
    public JobDetail pauseJob(@PathVariable Id id) {
        final var jobWithRun = repository.find(id);

        return jobToJobDetail(service.transition(jobWithRun, State.Paused));
    }

    @PostMapping("/jobs/{id}/start")
    public JobDetail startJob(@PathVariable Id id) {
        final var jobWithRun = repository.find(id);

        return jobToJobDetail(service.transition(jobWithRun, State.Ready));
    }

    @PostMapping("/jobs/{id}/cancel")
    public JobDetail cancelJob(@PathVariable Id id) {
        final var jobWithRun = repository.find(id);

        return jobToJobDetail(service.transition(jobWithRun, State.Canceled));
    }

    @PostMapping("/jobs/{id}/updateResult")
    public JobDetail updateJobResult(@PathVariable Id id, @RequestBody SaveJobResultPayload payload) {
        final var jobWithRun = repository.find(id);
        if (!(jobWithRun.job().data instanceof InferenceJobData inferenceJobData))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The job data is not an instance of InferenceJobData");

        System.out.println("positionsMap in controller: " + payload.positions());
        Map<Key, Position> positionsMap = extractPositions(payload.positions());

        final InferenceEdit edit = payload.isFinal() ? null : payload.edit();

        final JobWithRun newJobWithRun = jobExecutorService.continueRSDToCategoryProcessing(
            jobWithRun,
            inferenceJobData,
            edit,
            payload.isFinal(),
            payload.layoutType(),
            positionsMap
        );

        return jobToJobDetail(service.transition(newJobWithRun, payload.isFinal() ? State.Finished : State.Waiting));
    }

    @Nullable
    private Map<Key, Position> extractPositions(List<PositionUpdate> positionUpdates) {
        if (positionUpdates == null)
            return null;

        Map<Key, Position> positionsMap = new HashMap<>();
        if (positionUpdates != null) {
            for (PositionUpdate positionUpdate : positionUpdates) {
                positionsMap.put(positionUpdate.key(), positionUpdate.position());
            }
        }
        return positionsMap;
    }

    private record SaveJobResultPayload(
        @Nullable boolean isFinal,
        @Nullable InferenceEdit edit,
        @Nullable LayoutType layoutType,
        @Nullable List<PositionUpdate> positions
    ) {}

    private record PositionUpdate(
        Key key,
        Position position
    ) {}

    private JobDetail jobToJobDetail(JobWithRun job) {
        final var payload = actionController.actionPayloadToDetail(job.job().payload, job.run().categoryId);

        return JobDetail.create(job, payload);
    }

    private record JobDetail(
        Id id,
        Id categoryId,
        Id runId,
        @Nullable Id actionId,
        String label,
        Date createdAt,
        Job.State state,
        ActionPayloadDetail payload,
        @Nullable JobData data,
        @Nullable Serializable error
    ) implements IEntity {
        public static JobDetail create(JobWithRun jobWithRun, ActionPayloadDetail payload) {
            final var job = jobWithRun.job();
            final var run = jobWithRun.run();

            return new JobDetail(job.id(), run.categoryId, run.id(), run.actionId, job.label, job.createdAt, job.state, payload, job.data, job.error);
        }
    }

    @GetMapping("/schema-categories/{categoryId}/sessions")
    public List<Session> getAllSessions(@PathVariable Id categoryId) {
        return repository.findAllSessionsInCategory(categoryId);
    }

    @PostMapping("/schema-categories/{categoryId}/sessions")
    public Session createSession(@PathVariable Id categoryId) {
        return service.createSession(categoryId);
    }

}
