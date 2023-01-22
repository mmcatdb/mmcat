package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.utils.DataResult;
import cz.cuni.matfyz.core.utils.io.UrlInputStreamProvider;
import cz.cuni.matfyz.integration.processes.JsonLdToInstance;
import cz.cuni.matfyz.server.builder.CategoryBuilder;
import cz.cuni.matfyz.server.builder.MappingBuilder;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.job.Job;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.repository.JobRepository;
import cz.cuni.matfyz.server.service.WrapperService.WrapperCreationErrorException;
import cz.cuni.matfyz.server.service.WrapperService.WrapperNotFoundException;
import cz.cuni.matfyz.server.utils.UserStore;
import cz.cuni.matfyz.transformations.processes.DatabaseToInstance;
import cz.cuni.matfyz.transformations.processes.InstanceToDatabase;

import java.util.List;
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
    private DataSourceService dataSourceService;

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

        final var nextJobData = jobQueue.poll();
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
                case JsonLdToCategory:
                    jsonLdToCategoryProcess(job, store);
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
        final var instance = store.getCategory(job.categoryId);
        final var result = modelToCategoryAlgorithm(job, instance).join();

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
        final var logicalModel = logicalModelService.find(job.logicalModelId);
        final var database = databaseService.find(logicalModel.databaseId);

        final var pullWrapper = wrapperService.getControlWrapper(database).getPullWrapper();
        final var mappingWrappers = mappingService.findAll(job.logicalModelId);

        var result = new DataResult<InstanceCategory>(instance);
        for (final var mappingWrapper : mappingWrappers) {
            final var mapping = createMapping(mappingWrapper, job.categoryId);
            final var process = new DatabaseToInstance();
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
        final var instance = store.getCategory(job.categoryId);

        /*
        if (instance == null) {
            setJobStatus(job, Job.Status.Canceled);
            return;
        }
        */

        final var result = categoryToModelAlgorithm(job, instance).join();

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
        final var logicalModel = logicalModelService.find(job.logicalModelId);
        final var database = databaseService.find(logicalModel.databaseId);
        final List<Mapping> mappings = mappingService.findAll(job.logicalModelId).stream()
            .map(wrapper -> createMapping(wrapper, job.categoryId))
            .toList();

        final var control = wrapperService.getControlWrapper(database);
        
        final var output = new StringBuilder();
        for (final var mapping : mappings) {
            final var ddlWrapper = control.getDDLWrapper();
            final var icWrapper = control.getICWrapper();
            final var dmlWrapper = control.getDMLWrapper();

            final var process = new InstanceToDatabase();
            process.input(mapping, mappings, instance, ddlWrapper, dmlWrapper, icWrapper);

            final var result = process.run();
            if (!result.status)
                return CompletableFuture.completedFuture(new DataResult<String>(null, result.error));

            control.execute(result.data.statements());

            output.append(result.data.statementsAsString() + "\n");
        }

        return CompletableFuture.completedFuture(new DataResult<>(output.toString()));
    }
    
    @Async("jobExecutor")
    private void jsonLdToCategoryProcess(Job job, UserStore store) throws WrapperNotFoundException, WrapperCreationErrorException {
        final var instance = store.getCategory(job.categoryId);
        final var result = jsonLdToCategoryAlgorithm(job, instance).join();

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
    private CompletableFuture<DataResult<InstanceCategory>> jsonLdToCategoryAlgorithm(Job job, InstanceCategory instance) throws WrapperNotFoundException, WrapperCreationErrorException {
        final var dataSource = dataSourceService.find(job.dataSourceId);
        final var inputStreamProvider = new UrlInputStreamProvider(dataSource.url);

        final var schemaCategoryWrapper = categoryService.find(job.categoryId);
        final var schemaCategory = new CategoryBuilder()
            .setCategoryWrapper(schemaCategoryWrapper)
            .build();

        final var process = new JsonLdToInstance();
        process.input(schemaCategory, instance, inputStreamProvider);

        final var result = process.run();

        return CompletableFuture.completedFuture(result);
    }

    private Mapping createMapping(MappingWrapper mappingWrapper, Id categoryId) {
        final var categoryWrapper = categoryService.find(categoryId);

        return new MappingBuilder()
            .setMappingWrapper(mappingWrapper)
            .setCategoryWrapper(categoryWrapper)
            .build();
    }

}
