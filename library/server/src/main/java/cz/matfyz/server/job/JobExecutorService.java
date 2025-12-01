package cz.matfyz.server.job;

import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.server.evolution.EvolutionService;
import cz.matfyz.server.evolution.SchemaEvolutionPayload;
import cz.matfyz.server.inference.InferencePayload;
import cz.matfyz.server.inference.InferenceService;
import cz.matfyz.server.instance.CategoryToModelPayload;
import cz.matfyz.server.instance.InstanceService;
import cz.matfyz.server.instance.ModelToCategoryPayload;
import cz.matfyz.server.job.JobRepository.JobInfo;
import cz.matfyz.server.utils.entity.Id;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class JobExecutorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobExecutorService.class);

    @Autowired
    private JobRepository repository;

    @Autowired
    private JobService service;

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private EvolutionService evolutionService;

    @Autowired
    private InferenceService inferenceService;

    // The jobs in general can not run in parallel (for example, one can export from the instance category the second one is importing into).
    // There is an opportunity for optimalizaiton (only importing / only exporting jobs can run in parallel) but it would require synchronization on the instance level in the transformation algorithms.

    // This method will be executed 2 seconds after the previous execution finished.
    @Scheduled(fixedDelay = 2000)
    public void processAllRuns() {
        final var activeRunIds = repository.findAllActiveRunIds();
        for (final var runId : activeRunIds)
            processRun(runId);
    }

    private void processRun(Id runId) {
        final var run = repository.findRunWithJobs(runId);
        final @Nullable JobInfo nextJob = service.getNextReadyJob(run.jobs());
        if (nextJob != null) {
            executeJob(nextJob.id());
            return;
        }

        // The job can't be executed, so we mark the run as inactive. As soon as any job in the run changes, the run will be marked as active again.
        repository.deactivateRun(runId);
    }

    private void executeJob(Id jobId) {
        final var jobWithRun = repository.find(jobId);
        final var run = jobWithRun.run();
        final var job = jobWithRun.job();

        if (job.state != Job.State.Ready) {
            LOGGER.warn("Job { id: {}, name: '{}' } is not ready.", job.id(), job.label);
            return;
        }

        job.state = Job.State.Running;
        repository.save(job);
        LOGGER.info("Job { id: {}, name: '{}' } started.", job.id(), job.label);

        try {
            processJobByType(run, job);

            //Thread.sleep(JOB_DELAY_IN_SECONDS * 1000);

            job.state = job.payload.isFinishedAutomatically() ? Job.State.Finished : Job.State.Waiting;

            repository.save(job);
            LOGGER.info("Job { id: {}, name: '{}' } finished.", job.id(), job.label);
        }
        catch (Exception e) {
            final NamedException finalException = e instanceof NamedException namedException ? namedException : new OtherException(e);

            LOGGER.error(String.format("Job { id: %s, name: '%s' } failed.", job.id(), job.label), finalException);
            job.state = Job.State.Failed;
            job.error = finalException.toSerializedException();
            repository.save(job);
        }
    }

    private void processJobByType(Run run, Job job) {
        switch (job.payload) {
            case ModelToCategoryPayload p -> instanceService.startModelToCategoryJob(run, job, p);
            case CategoryToModelPayload p -> instanceService.startCategoryToModelJob(run, job, p);
            case SchemaEvolutionPayload p -> evolutionService.startJob(run, job, p);
            case InferencePayload p -> inferenceService.startJob(job, p);
            default -> throw new UnsupportedOperationException("Job payload type not supported: " + job.payload.getClass().getSimpleName() + ".");
        }
    }

}
