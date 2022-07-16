package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.utils.RunJobData;
import cz.cuni.matfyz.server.utils.UserStore;
import cz.cuni.matfyz.transformations.processes.DatabaseToInstance;
import cz.cuni.matfyz.transformations.processes.InstanceToDatabase;
import cz.cuni.matfyz.abstractWrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractWrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractWrappers.AbstractPushWrapper;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.utils.DataResult;
import cz.cuni.matfyz.server.builder.SchemaBuilder;
import cz.cuni.matfyz.server.entity.Job;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.repository.JobRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jachym.bartik
 */
@Service
public class AsyncJobService {

    private static final int JOB_DELAY_IN_SECONDS = 2;

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncJobService.class);

    /*
     * The jobs in general can not run in parallel (for example, one can export from the instance category the second one is importing into).
     * There is a prostor for an optimalizaiton (only importing / only exporting jobs can run in parallel) but it would require a synchronization on the instance level in the transformation algorithms.
     */
    private Queue<RunJobData> jobQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean jobIsRunning = false;

    @Autowired
    private JobRepository repository;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private SchemaCategoryService categoryService;

    @Autowired
    private WrapperService wrapperService;

    @Autowired
    private ModelService modelService;

    public void runJob(Job job, UserStore store) {

        jobQueue.add(new RunJobData(job, store));
        
        LOGGER.info("Job { id: {}, name: '{}' } placed to the queue.", job.id, job.name);
        
        tryStartNextJob(false);
    }

    private synchronized void tryStartNextJob(boolean currentIsEnding) {
        if (jobIsRunning && !currentIsEnding)
            return;

        var nextJobData = jobQueue.poll();
        if (nextJobData == null) {
            jobIsRunning = false;
            return;
        }
        
        jobIsRunning = true;
        processJob(nextJobData.job, nextJobData.store);
    }

    @Async("jobExecutor")
    public void processJob(Job job, UserStore store) {
        LOGGER.info("Job { id: {}, name: '{}' } started.", job.id, job.name);
        
        try {
            switch (job.type) {
                case CategoryToModel:
                    categoryToModelProcess(job, store);
                    break;
                case ModelToCategory:
                    modelToCategoryProcess(job, store);
                    break;
            }
        }
        catch (Exception exception) {
            LOGGER.error(String.format("Job { id: %d, name: '%s' } interrupted.", job.id, job.name), exception);
            setJobStatus(job, Job.Status.Canceled);
        }

        LOGGER.info("Job { id: {}, name: '{}' } finished.", job.id, job.name);
        tryStartNextJob(true);
    }

    private void setJobStatus(Job job, Job.Status status) {
        job.status = status;
        repository.updateJSONValue(job);
    }

    @Async("jobExecutor")
    private void modelToCategoryProcess(Job job, UserStore store) throws Exception {
        var instance = store.getInstance(job.schemaId);
        var result = modelToCategoryAlgorithm(job, instance).join();

        if (result.status) {
            if (instance == null)
                store.setInstance(job.schemaId, result.data);
            setJobStatus(job, Job.Status.Finished);
        }
        else {
            setJobStatus(job, Job.Status.Canceled);
        }
    }

    @Async("jobExecutor")
    private CompletableFuture<DataResult<InstanceCategory>> modelToCategoryAlgorithm(Job job, InstanceCategory instance) throws Exception {
        var mappingWrapper = mappingService.find(job.mappingId);
        var mapping = createMapping(mappingWrapper);

        Database database = databaseService.find(mappingWrapper.databaseId);
        AbstractPullWrapper pullWrapper = wrapperService.getPullWraper(database);

        var process = new DatabaseToInstance();
        process.input(mapping, instance, pullWrapper);

        var result = process.run();
        Thread.sleep(JOB_DELAY_IN_SECONDS * 1000);

        return CompletableFuture.completedFuture(result);
    }

    @Async("jobExecutor")
    private void categoryToModelProcess(Job job, UserStore store) throws Exception {
        var instance = store.getInstance(job.schemaId);

        if (instance == null) {
            setJobStatus(job, Job.Status.Canceled);
            return;
        }

        var result = categoryToModelAlgorithm(job, instance).join();

        if (result.status) {
            modelService.createNew(store, job, job.name, result.data);
            setJobStatus(job, Job.Status.Finished);
        }
        else {
            setJobStatus(job, Job.Status.Canceled);
        }
    }

    @Async("jobExecutor")
    private CompletableFuture<DataResult<String>> categoryToModelAlgorithm(Job job, InstanceCategory instance) throws Exception {
        var mappingWrapper = mappingService.find(job.mappingId);
        var mapping = createMapping(mappingWrapper);

        Database database = databaseService.find(mappingWrapper.databaseId);
        AbstractDDLWrapper ddlWrapper = wrapperService.getDDLWrapper(database);
        AbstractPushWrapper pushWrapper = wrapperService.getPushWrapper(database);

        var process = new InstanceToDatabase();
        process.input(mapping, instance, ddlWrapper, pushWrapper);

        var result = process.run();
        Thread.sleep(JOB_DELAY_IN_SECONDS * 1000);

        return CompletableFuture.completedFuture(result);
    }

    private Mapping createMapping(MappingWrapper mappingWrapper) {
        var categoryWrapper = categoryService.find(mappingWrapper.categoryId);

        var mapping = new SchemaBuilder()
            .setMappingWrapper(mappingWrapper)
            .setCategoryWrapper(categoryWrapper)
            .build();
        
        return mapping;
    }

}
