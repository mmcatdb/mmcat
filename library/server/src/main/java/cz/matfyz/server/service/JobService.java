package cz.matfyz.server.service;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.Action;
import cz.matfyz.server.entity.action.ActionPayload;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.job.Run;
import cz.matfyz.server.repository.JobRepository;
import cz.matfyz.server.repository.JobRepository.JobWithRun;

import com.mongodb.lang.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jachym.bartik
 */
@Service
public class JobService {

    @Autowired
    private JobRepository repository;

    public JobWithRun createRun(Action action) {
        return createRun(action.categoryId, action.id, action.label, action.payload);
    }

    public JobWithRun createRun(Id categoryId, @Nullable Id actionId, String label, ActionPayload payload) {
        final var run = Run.createNew(categoryId, actionId);
        final var job = Job.createNew(run.id, label, payload);

        repository.save(run);
        repository.save(job);

        return new JobWithRun(job, run);
    }

    public JobWithRun createRestartedJob(JobWithRun jobWithRun) {
        final var job = jobWithRun.job();
        final var newJob = Job.createNew(job.runId, job.label, job.payload);

        repository.save(newJob);

        return new JobWithRun(newJob, jobWithRun.run());
    }

    public JobWithRun cancel(JobWithRun jobWithRun) {
        final var job = jobWithRun.job();

        job.state = Job.State.Canceled;
        repository.save(job);

        return new JobWithRun(job, jobWithRun.run());
    }

}
