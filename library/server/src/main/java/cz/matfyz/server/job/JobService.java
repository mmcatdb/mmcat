package cz.matfyz.server.job;

import cz.matfyz.server.utils.RequestContext;
import cz.matfyz.server.utils.entity.Id;
import cz.matfyz.server.job.Job.State;
import cz.matfyz.server.job.JobRepository.JobInfo;
import cz.matfyz.server.job.JobRepository.JobWithRun;
import cz.matfyz.server.job.JobRepository.RunWithJobs;
import cz.matfyz.server.exception.InvalidTransitionException;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    @Autowired
    private RequestContext request;

    @Autowired
    private JobRepository repository;

    public RunWithJobs createRun(Action action) {
        return createRunInner(action.categoryId, action.label, action.payloads, action.id());
    }

    public RunWithJobs createRun(Id categoryId, String label, List<JobPayload> payloads) {
        return createRunInner(categoryId, label, payloads, null);
    }

    private RunWithJobs createRunInner(Id categoryId, String label, List<JobPayload> payloads, @Nullable Id actionId) {
        final var run = Run.create(categoryId, label, actionId, request.getSessionId());
        repository.save(run);

        final var jobs = new ArrayList<Job>();
        int index = 0;
        for (final var payload : payloads) {
            // TODO find a better way to get the job label. Probably from the payload? Or some other configuration object?
            final var job = Job.createNew(run.id(), index++, payload.getClass().getSimpleName(), payload, payload.isStartedAutomatically());
            repository.save(job);
            jobs.add(job);
        }

        return new RunWithJobs(run, jobs.stream().map(JobInfo::fromJob).toList());
    }

    public JobWithRun createRestartedJob(JobWithRun jobWithRun) {
        final var job = jobWithRun.job();
        // If the job is restarted, it is always started automatically.
        final var newJob = Job.createNew(job.runId, job.index, job.label, job.payload, true);

        repository.save(newJob);

        return new JobWithRun(newJob, jobWithRun.run());
    }

    public void enableJob(Job job) {
        if (job.state != State.Disabled)
            throw InvalidTransitionException.job(job.id(), job.state, State.Ready);

        job.state = State.Ready;
        repository.save(job);
    }

    public void disableJob(Job job) {
        if (job.state != State.Ready)
            throw InvalidTransitionException.job(job.id(), job.state, State.Disabled);

        job.state = State.Disabled;
        repository.save(job);
    }

    public Session createSession(Id categoryId) {
        final var session = Session.createNew(categoryId);
        repository.save(session);

        return session;
    }

    /**
     * All jobs are expected to be in the same run.
     */
    public @Nullable JobInfo getNextReadyJob(List<JobInfo> jobs) {
        final var sortedJobs = jobs.stream().sorted(Job::compareInRun).toList();
        final var activeJobs = new ArrayList<JobInfo>();

        for (int i = 1; i < sortedJobs.size(); i++)
            if (sortedJobs.get(i).index() != sortedJobs.get(i - 1).index())
                activeJobs.add(sortedJobs.get(i - 1));
        activeJobs.add(sortedJobs.get(sortedJobs.size() - 1));

        for (final var job : activeJobs) {
            if (job.state() == State.Ready)
                return job;
            if (job.state() == State.Finished)
                continue;

            break;
        }

        return null;
    }

}
