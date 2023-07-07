package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.core.exception.NamedException;
import cz.cuni.matfyz.core.exception.OtherException;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.utils.io.UrlInputStreamProvider;
import cz.cuni.matfyz.integration.processes.JsonLdToCategory;
import cz.cuni.matfyz.server.builder.MappingBuilder;
import cz.cuni.matfyz.server.builder.SchemaCategoryContext;
import cz.cuni.matfyz.server.configuration.ServerProperties;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.entity.datasource.DataSource;
import cz.cuni.matfyz.server.entity.job.Job;
import cz.cuni.matfyz.server.entity.job.payload.CategoryToModelPayload;
import cz.cuni.matfyz.server.entity.job.payload.JsonLdToCategoryPayload;
import cz.cuni.matfyz.server.entity.job.payload.ModelToCategoryPayload;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.cuni.matfyz.server.repository.JobRepository;
import cz.cuni.matfyz.server.utils.UserStore;
import cz.cuni.matfyz.transformations.processes.DatabaseToInstance;
import cz.cuni.matfyz.transformations.processes.InstanceToDatabase;

import java.io.Serializable;
import java.util.List;
import java.util.Queue;
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

    @Autowired
    private ServerProperties server;

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
            processJobByType(job, store);
            LOGGER.info("Job { id: {}, name: '{}' } finished.", job.id, job.label);
            setJobState(job, Job.State.Finished, null);
        }
        catch (Exception e) {
            final NamedException finalException = e instanceof NamedException namedException ? namedException : new OtherException(e);
            
            LOGGER.error(String.format("Job { id: %s, name: '%s' } failed.", job.id, job.label), finalException);
            setJobState(job, Job.State.Failed, finalException.toSerializedException());
        }

        tryStartNextJob(true);
    }

    private void setJobState(Job job, Job.State state, Serializable data) {
        job.state = state;
        job.data = data;
        repository.updateJsonValue(job);
    }

    @Async("jobExecutor")
    private void processJobByType(Job job, UserStore store) {
        if (job.payload instanceof CategoryToModelPayload categoryToModelPayload)
            categoryToModelAlgorithm(job, categoryToModelPayload, store);
        else if (job.payload instanceof ModelToCategoryPayload modelToCategoryPayload)
            modelToCategoryAlgorithm(job, modelToCategoryPayload, store);
        else if (job.payload instanceof JsonLdToCategoryPayload jsonLdToCategoryPayload)
            jsonLdToCategoryAlgorithm(job, jsonLdToCategoryPayload, store);

        //Thread.sleep(JOB_DELAY_IN_SECONDS * 1000);
    }

    @Async("jobExecutor")
    private void modelToCategoryAlgorithm(Job job, ModelToCategoryPayload payload, UserStore store) {
        final LogicalModel logicalModel = logicalModelService.find(payload.logicalModelId());
        final Database database = databaseService.find(logicalModel.databaseId);

        final AbstractPullWrapper pullWrapper = wrapperService.getControlWrapper(database).getPullWrapper();
        final List<MappingWrapper> mappingWrappers = mappingService.findAll(payload.logicalModelId());

        InstanceCategory instance = store.getCategory(job.categoryId);

        for (final MappingWrapper mappingWrapper : mappingWrappers) {
            final Mapping mapping = createMapping(mappingWrapper, job.categoryId);
            instance = new DatabaseToInstance().input(mapping, instance, pullWrapper).run();
        }

        store.setInstance(job.categoryId, instance);
    }

    @Async("jobExecutor")
    private void categoryToModelAlgorithm(Job job, CategoryToModelPayload payload, UserStore store) {
        final InstanceCategory instance = store.getCategory(job.categoryId);

        final LogicalModel logicalModel = logicalModelService.find(payload.logicalModelId());
        final Database database = databaseService.find(logicalModel.databaseId);
        final List<Mapping> mappings = mappingService.findAll(payload.logicalModelId()).stream()
            .map(wrapper -> createMapping(wrapper, job.categoryId))
            .toList();

        final AbstractControlWrapper control = wrapperService.getControlWrapper(database);
        
        final var output = new StringBuilder();
        for (final Mapping mapping : mappings) {
            final var result = new InstanceToDatabase()
                .input(
                    mapping,
                    mappings,
                    instance,
                    control.getDDLWrapper(),
                    control.getDMLWrapper(),
                    control.getICWrapper()
                )
                .run();

            output.append(result.statementsAsString() + "\n");

            // TODO - find a better way how to execute the changes (currently its too likely to fail)
            if (server.executeModels()) {
                LOGGER.info("Start executing models ...");
                control.execute(result.statements());
                LOGGER.info("... models executed.");
            }
        }
        
        modelService.createNew(store, job, job.label, output.toString());
    }
    
    @Async("jobExecutor")
    private void jsonLdToCategoryAlgorithm(Job job, JsonLdToCategoryPayload payload, UserStore store) {
        final InstanceCategory instance = store.getCategory(job.categoryId);

        final DataSource dataSource = dataSourceService.find(payload.dataSourceId());
        final var inputStreamProvider = new UrlInputStreamProvider(dataSource.url);

        final SchemaCategoryWrapper schemaWrapper = categoryService.find(job.categoryId);
        final var context = new SchemaCategoryContext();
        final SchemaCategory schema = schemaWrapper.toSchemaCategory(context);

        final var newInstance = new JsonLdToCategory().input(schema, instance, inputStreamProvider).run();
        store.setInstance(job.categoryId, newInstance);
    }

    private Mapping createMapping(MappingWrapper mappingWrapper, Id categoryId) {
        final var categoryWrapper = categoryService.find(categoryId);

        return new MappingBuilder()
            .setMappingWrapper(mappingWrapper)
            .setCategoryWrapper(categoryWrapper)
            .build();
    }

}
