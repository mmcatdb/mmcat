package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.utils.UserStore;
import cz.cuni.matfyz.transformations.processes.DatabaseToInstance;
import cz.cuni.matfyz.transformations.processes.InstanceToDatabase;
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
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jachym.bartik
 */
@Service
public class AsyncJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncJobService.class);

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

    @Async("jobExecutor")
    public void runJob(Job job, UserStore store) {
        LOGGER.info("RUN JOB");
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
            LOGGER.error("Job " + job.id + " was interrupted.", exception);
            setJobStatus(job, Job.Status.Canceled);
        }
        LOGGER.info("RUN JOB END");
    }

    private void setJobStatus(Job job, Job.Status status) {
        job.status = status;
        repository.updateJSONValue(job);
    }

    @Async("jobExecutor")
    private void modelToCategoryProcess(Job job, UserStore store) throws Exception {
        var defaultInstance = store.getDefaultInstace();
        var result = modelToCategoryAlgorithm(job, defaultInstance).join();

        if (result.status) {
            //store.addInstance(job.id, result.data);
            if (defaultInstance == null)
                store.setDefaultInstance(result.data);
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
        Thread.sleep(2 * 1000);

        return CompletableFuture.completedFuture(result);
    }

    @Async("jobExecutor")
    private void categoryToModelProcess(Job job, UserStore store) throws Exception {
        var defaultInstance = store.getDefaultInstace();
        var result = categoryToModelAlgorithm(job, defaultInstance).join();

        if (result.status) {
            // TODO Do something with the result.
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
        AbstractPushWrapper pushWrapper = wrapperService.getPushWrapper(database);

        var process = new InstanceToDatabase();
        process.input(mapping, instance, pushWrapper);

        var result = process.run();
        Thread.sleep(2 * 1000);

        // TODO The DDL and IC algorithms should be here.

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
