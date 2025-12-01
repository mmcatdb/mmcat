package cz.matfyz.server.job;

import cz.matfyz.server.utils.RequestContext;
import cz.matfyz.server.utils.entity.IEntity;
import cz.matfyz.server.utils.entity.Id;
import cz.matfyz.server.workflow.WorkflowService;
import cz.matfyz.server.job.ActionController.JobPayloadDetail;
import cz.matfyz.server.job.JobRepository.JobInfo;
import cz.matfyz.server.job.JobRepository.JobWithRun;
import cz.matfyz.server.job.JobRepository.RunWithJobs;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobController {

    @Autowired
    private RequestContext request;

    @Autowired
    private JobRepository repository;

    @Autowired
    private JobService service;

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private ActionController actionController;

    @Autowired
    private WorkflowService workflowService;

    @GetMapping("/schema-categories/{categoryId}/jobs")
    public List<JobDetail> getAllJobsInCategory(@PathVariable Id categoryId) {
        final var jobs = repository.findAllInCategory(categoryId, request.getSessionId());

        return jobs.stream().map(this::jobToJobDetail).toList();
    }

    @GetMapping("/jobs/{id}")
    public JobDetail getJob(@PathVariable Id id) {
        return jobToJobDetail(repository.find(id));
    }

    @PostMapping("/actions/{actionId}/jobs")
    public RunDetail createRun(@PathVariable Id actionId) {
        final var action = actionRepository.find(actionId);

        return RunDetail.create(service.createRun(action));
    }

    @PostMapping("/jobs/{id}/restart")
    public JobDetail createRestartedJob(@PathVariable Id id) {
        final var oldJob = repository.find(id);
        final var newJob = service.createRestartedJob(oldJob);

        // We have to update all workflows that depend on the job.
        workflowService.updateWorkflowsWithRestartedJob(oldJob.job(), newJob.job());

        return jobToJobDetail(newJob);
    }

    @PostMapping("/jobs/{id}/enable")
    public JobDetail enableJob(@PathVariable Id id) {
        final var jobWithRun = repository.find(id);
        service.enableJob(jobWithRun.job());

        return jobToJobDetail(jobWithRun);
    }

    @PostMapping("/jobs/{id}/disable")
    public JobDetail disableJob(@PathVariable Id id) {
        final var jobWithRun = repository.find(id);
        service.disableJob(jobWithRun.job());

        return jobToJobDetail(jobWithRun);
    }

    public JobDetail jobToJobDetail(JobWithRun job) {
        final var payload = actionController.jobPayloadToDetail(job.job().payload, job.run().categoryId);

        return JobDetail.create(job, payload);
    }

    public record JobDetail(
        Id id,
        int index,
        String label,
        Date createdAt,
        Job.State state,
        JobPayloadDetail payload,
        @Nullable JobData data,
        @Nullable Serializable error,
        Id runId,
        Id categoryId,
        String runLabel,
        @Nullable Id actionId
    ) implements IEntity {
        public static JobDetail create(JobWithRun jobWithRun, JobPayloadDetail payload) {
            final var job = jobWithRun.job();
            final var run = jobWithRun.run();
            return new JobDetail(job.id(), job.index, job.label, job.createdAt, job.state, payload, job.data, job.error, run.id(), run.categoryId, run.label, run.actionId);
        }
    }

    private record RunDetail(
        Id id,
        Id categoryId,
        @Nullable Id actionId,
        String label,
        List<JobInfo> jobs
    ) implements IEntity {
        public static RunDetail create(RunWithJobs runWithJobs) {
            final var run = runWithJobs.run();
            return new RunDetail(run.id(), run.categoryId, run.actionId, run.label, runWithJobs.jobs());
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
