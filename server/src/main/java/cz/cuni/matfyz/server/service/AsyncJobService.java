package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPushWrapper;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.utils.DataResult;
import cz.cuni.matfyz.server.builder.MappingBuilder;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.entity.job.Job;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.repository.JobRepository;
import cz.cuni.matfyz.server.service.WrapperService.WrapperCreationErrorException;
import cz.cuni.matfyz.server.service.WrapperService.WrapperNotFoundException;
import cz.cuni.matfyz.server.utils.UserStore;
import cz.cuni.matfyz.transformations.processes.DatabaseToInstance;
import cz.cuni.matfyz.transformations.processes.InstanceToDatabase;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author jachym.bartik
 */
@Service
public class AsyncJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncJobService.class);

    /*
     * The jobs in general can not run in parallel (for example, one can export from the instance category the second one is importing into).
     * There is a prostor for an optimalizaiton (only importing / only exporting jobs can run in parallel) but it would require a synchronization on the instance level in the transformation algorithms.
     */

    private record RunJobData(Job job, UserStore store) {}

    private Queue<RunJobData> jobQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean jobIsRunning = false;

    @Autowired
    private JobRepository repository;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private LogicalModelService logicalModelService;

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
        
        LOGGER.info("Job { id: {}, name: '{}' } placed to the queue.", job.id, job.label);
        
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
        LOGGER.info("Job { id: {}, name: '{}' } started.", job.id, job.label);
        
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
            LOGGER.error(String.format("Job { id: %s, name: '%s' } interrupted.", job.id, job.label), exception);
            setJobStatus(job, Job.Status.Canceled);
        }

        LOGGER.info("Job { id: {}, name: '{}' } finished.", job.id, job.label);
        tryStartNextJob(true);
    }

    private void setJobStatus(Job job, Job.Status status) {
        job.status = status;
        repository.updateJSONValue(job);
    }

    @Async("jobExecutor")
    private void modelToCategoryProcess(Job job, UserStore store) throws WrapperNotFoundException, WrapperCreationErrorException {
        var instance = store.getCategory(job.categoryId);
        var result = modelToCategoryAlgorithm(job, instance).join();

        if (result.status) {
            if (instance == null)
                store.setInstance(job.categoryId, result.data);
            setJobStatus(job, Job.Status.Finished);
        }
        else {
            setJobStatus(job, Job.Status.Canceled);
        }
    }

    @Async("jobExecutor")
    private CompletableFuture<DataResult<InstanceCategory>> modelToCategoryAlgorithm(Job job, InstanceCategory instance) throws WrapperNotFoundException, WrapperCreationErrorException {       
        var logicalModel = logicalModelService.find(job.logicalModelId);
        Database database = databaseService.find(logicalModel.databaseId);

        AbstractPullWrapper pullWrapper = wrapperService.getPullWraper(database);
        var mappingWrappers = mappingService.findAll(job.logicalModelId);

        var result = new DataResult<InstanceCategory>(instance);
        for (var mappingWrapper : mappingWrappers) {
            var mapping = createMapping(mappingWrapper, logicalModel);
            var process = new DatabaseToInstance();
            process.input(mapping, result.data, pullWrapper);

            result = process.run();
            if (!result.status)
                break;
        }
        //Thread.sleep(JOB_DELAY_IN_SECONDS * 1000);

        return CompletableFuture.completedFuture(result);
    }

    @Async("jobExecutor")
    private void categoryToModelProcess(Job job, UserStore store) throws WrapperNotFoundException, WrapperCreationErrorException {
        var instance = store.getCategory(job.categoryId);

        /*
        if (instance == null) {
            setJobStatus(job, Job.Status.Canceled);
            return;
        }
        */

        var result = categoryToModelAlgorithm(job, instance).join();

        if (result.status) {
            modelService.createNew(store, job, job.label, result.data);
            setJobStatus(job, Job.Status.Finished);
        }
        else {
            setJobStatus(job, Job.Status.Canceled);
        }
    }

    @Async("jobExecutor")
    private CompletableFuture<DataResult<String>> categoryToModelAlgorithm(Job job, InstanceCategory instance) throws WrapperNotFoundException, WrapperCreationErrorException {
        var logicalModel = logicalModelService.find(job.logicalModelId);
        Database database = databaseService.find(logicalModel.databaseId);

        AbstractDDLWrapper ddlWrapper = wrapperService.getDDLWrapper(database);
        AbstractPushWrapper pushWrapper = wrapperService.getPushWrapper(database);
        var mappingWrappers = mappingService.findAll(job.logicalModelId);

        var output = new StringBuilder();
        for (var mappingWrapper : mappingWrappers) {
            var mapping = createMapping(mappingWrapper, logicalModel);
            var process = new InstanceToDatabase();
            process.input(mapping, instance, ddlWrapper, pushWrapper);

            var result = process.run();
            if (result.status)
                return CompletableFuture.completedFuture(result);

            output.append(result.data + "\n");
        }

        return CompletableFuture.completedFuture(new DataResult<>(output.toString()));
    }

    private Mapping createMapping(MappingWrapper mappingWrapper, LogicalModel logicalModel) {
        var categoryWrapper = categoryService.find(logicalModel.categoryId);

        return new MappingBuilder()
            .setMappingWrapper(mappingWrapper)
            .setCategoryWrapper(categoryWrapper)
            .build();
    }

}
