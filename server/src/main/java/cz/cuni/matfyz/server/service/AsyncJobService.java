package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.server.utils.UserStore;
import cz.cuni.matfyz.transformations.processes.DatabaseToInstance;
import cz.cuni.matfyz.abstractWrappers.AbstractPullWrapper;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.utils.Result;
import cz.cuni.matfyz.server.builder.SchemaBuilder;
import cz.cuni.matfyz.server.entity.Database;
import cz.cuni.matfyz.server.entity.Job;
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
            // TODO
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
        catch (InterruptedException exception) {
            LOGGER.error("Job " + job.id + " was interrupted.", exception);
            setJobStatus(job, Job.Status.Canceled);
        }
        LOGGER.info("RUN JOB END");
    }

    @Async("jobExecutor")
    private CompletableFuture<Result<InstanceCategory>> modelToCategoryAlgorithm(Job job, InstanceCategory defaultInstance) throws InterruptedException {       
        var mappingWrapper = mappingService.find(job.mappingId);
        var categoryWrapper = categoryService.find(mappingWrapper.categoryId);

        var mapping = new SchemaBuilder()
            .setMappingWrapper(mappingWrapper)
            .setCategoryWrapper(categoryWrapper)
            .build();

        Database database = databaseService.find(mappingWrapper.databaseId);
        AbstractPullWrapper pullWrapper = wrapperService.getPullWraper(database);

        var process = new DatabaseToInstance();
        process.input(pullWrapper, mapping, defaultInstance);

        var result = process.run();
        Thread.sleep(2 * 1000);

        return CompletableFuture.completedFuture(result);
    }

    private void setJobStatus(Job job, Job.Status status) {
        job.status = status;
        repository.updateJSONValue(job);
    }

}
