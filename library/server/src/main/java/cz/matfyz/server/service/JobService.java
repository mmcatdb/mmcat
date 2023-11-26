package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.io.UrlInputStreamProvider;
import cz.matfyz.integration.processes.JsonLdToCategory;
import cz.matfyz.server.builder.MappingBuilder;
import cz.matfyz.server.builder.SchemaCategoryContext;
import cz.matfyz.server.configuration.ServerProperties;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.Action;
import cz.matfyz.server.entity.action.payload.CategoryToModelPayload;
import cz.matfyz.server.entity.action.payload.JsonLdToCategoryPayload;
import cz.matfyz.server.entity.action.payload.ModelToCategoryPayload;
import cz.matfyz.server.entity.database.Database;
import cz.matfyz.server.entity.datasource.DataSource;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.job.Run;
import cz.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.repository.JobRepository;
import cz.matfyz.server.repository.JobRepository.JobWithRun;
import cz.matfyz.transformations.processes.DatabaseToInstance;
import cz.matfyz.transformations.processes.InstanceToDatabase;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author jachym.bartik
 */
@Service
public class JobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);

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
    private ServerProperties server;

    public JobWithRun createRun(Action action) {
        final var run = Run.createNew(action.categoryId, action.id);
        final var job = Job.createNew(run.id, action.label, action.payload);

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

    // The jobs in general can not run in parallel (for example, one can export from the instance category the second one is importing into).
    // There is a space for an optimalizaiton (only importing / only exporting jobs can run in parallel) but it would require a synchronization on the instance level in the transformation algorithms.
    
    // This method will be executed 2 seconds after the previous execution finished.
    @Scheduled(fixedDelay = 2000)
    public void executeAllJobs() {
        final var readyIds = repository.findAllReadyIds();
        for (final var jobId : readyIds)
            executeJob(jobId);
    }

    private void executeJob(Id jobId) {
        final var jobWithRun = repository.find(jobId);
        final var run = jobWithRun.run();
        final var job = jobWithRun.job();

        if (job.state != Job.State.Ready) {
            LOGGER.info("Job { id: {}, name: '{}' } is not ready.", job.id, job.label);
            return;
        }

        LOGGER.info("Job { id: {}, name: '{}' } started.", job.id, job.label);
        
        try {
            processJobByType(run, job);
            LOGGER.info("Job { id: {}, name: '{}' } finished.", job.id, job.label);
            job.state = Job.State.Finished;
            job.data = null;
            repository.save(job);
        }
        catch (Exception e) {
            final NamedException finalException = e instanceof NamedException namedException ? namedException : new OtherException(e);
            
            LOGGER.error(String.format("Job { id: %s, name: '%s' } failed.", job.id, job.label), finalException);
            job.state = Job.State.Failed;
            job.data = finalException.toSerializedException();
        }

        // tryStartNextJob(true);
    }

    private void processJobByType(Run run, Job job) {
        if (job.payload instanceof CategoryToModelPayload categoryToModelPayload)
            categoryToModelAlgorithm(run, categoryToModelPayload);
        else if (job.payload instanceof ModelToCategoryPayload modelToCategoryPayload)
            modelToCategoryAlgorithm(run, modelToCategoryPayload);
        else if (job.payload instanceof JsonLdToCategoryPayload jsonLdToCategoryPayload)
            jsonLdToCategoryAlgorithm(run, jsonLdToCategoryPayload);

        //Thread.sleep(JOB_DELAY_IN_SECONDS * 1000);
    }

    private void modelToCategoryAlgorithm(Run run, ModelToCategoryPayload payload) {
        final LogicalModel logicalModel = logicalModelService.find(payload.logicalModelId());
        final Database database = databaseService.find(logicalModel.databaseId);

        final AbstractPullWrapper pullWrapper = wrapperService.getControlWrapper(database).getPullWrapper();
        final List<MappingWrapper> mappingWrappers = mappingService.findAll(payload.logicalModelId());

        // InstanceCategory instance = store.getCategory(run.categoryId);
        InstanceCategory instance = null;

        for (final MappingWrapper mappingWrapper : mappingWrappers) {
            final Mapping mapping = createMapping(mappingWrapper, run.categoryId);
            instance = new DatabaseToInstance().input(mapping, instance, pullWrapper).run();
        }

        // store.setInstance(run.categoryId, instance);
    }

    private void categoryToModelAlgorithm(Run run, CategoryToModelPayload payload) {
        // final InstanceCategory instance = store.getCategory(run.categoryId);
        InstanceCategory instance = null;

        final LogicalModel logicalModel = logicalModelService.find(payload.logicalModelId());
        final Database database = databaseService.find(logicalModel.databaseId);
        final List<Mapping> mappings = mappingService.findAll(payload.logicalModelId()).stream()
            .map(wrapper -> createMapping(wrapper, run.categoryId))
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

            // TODO - verzovat databáze - tj. vytvořit vždy novou databázi (v rámci stejného engine)
            //  - např. uživatel zvolí "my_db", tak vytvářet "my_db_1", "my_db_2" a podobně
            //  - resp. při opětovném spuštění to smazat a vytvořit znovu ...

            if (server.executeModels()) {
                LOGGER.info("Start executing models ...");
                control.execute(result.statements());
                LOGGER.info("... models executed.");
            }
        }
        
        // modelService.createNew(store, job, run, job.label, output.toString());
    }
    
    private void jsonLdToCategoryAlgorithm(Run run, JsonLdToCategoryPayload payload) {
        // final InstanceCategory instance = store.getCategory(run.categoryId);
        final InstanceCategory instance = null;

        final DataSource dataSource = dataSourceService.find(payload.dataSourceId());
        final var inputStreamProvider = new UrlInputStreamProvider(dataSource.url);

        final SchemaCategoryWrapper schemaWrapper = categoryService.find(run.categoryId);
        final var context = new SchemaCategoryContext();
        final SchemaCategory schema = schemaWrapper.toSchemaCategory(context);

        final var newInstance = new JsonLdToCategory().input(schema, instance, inputStreamProvider).run();
        // store.setInstance(run.categoryId, newInstance);
    }

    private Mapping createMapping(MappingWrapper mappingWrapper, Id categoryId) {
        final var categoryWrapper = categoryService.find(categoryId);

        return new MappingBuilder()
            .setMappingWrapper(mappingWrapper)
            .setCategoryWrapper(categoryWrapper)
            .build();
    }

}
