package cz.matfyz.server.controller;

import cz.matfyz.server.builder.GeneratedDataModel;
import cz.matfyz.server.controller.ActionController.ActionPayloadDetail;
import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.job.Session;
import cz.matfyz.server.entity.job.Job.State;
import cz.matfyz.server.repository.ActionRepository;
import cz.matfyz.server.repository.JobRepository;
import cz.matfyz.server.repository.JobRepository.JobWithRun;
import cz.matfyz.server.service.JobService;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class JobController {

    @Autowired
    private JobRepository repository;

    @Autowired
    private JobService service;

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private ActionController actionController;

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
        final var jobWithRun = repository.find(id);

        return jobToJobDetail(service.createRestartedJob(jobWithRun));
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

    private JobDetail jobToJobDetail(JobWithRun job) {
        final var payload = actionController.actionPayloadToDetail(job.job().payload);

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
        @Nullable Serializable data,
        @Nullable Serializable generatedDataModel
    ) implements IEntity {
        public static JobDetail create(JobWithRun jobWithRun, ActionPayloadDetail payload) {
            final var job = jobWithRun.job();
            final var run = jobWithRun.run();

            return new JobDetail(job.id, run.categoryId, run.id, run.actionId, job.label, job.createdAt, job.state, payload, job.data, job.generatedDataModel);
        }
    }

    @GetMapping("/schema-categories/{categoryId}/sessions")
    public List<Session> getAllSessions(@PathVariable Id categoryId) {
        return service.findAllSessions(categoryId);
    }

    @PostMapping("/schema-categories/{categoryId}/sessions")
    public Session createSession(@PathVariable Id categoryId) {
        return service.createSession(categoryId);
    }

}
