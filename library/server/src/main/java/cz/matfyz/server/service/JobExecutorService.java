package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer;
import cz.matfyz.core.utils.ArrayUtils;
import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.querying.QueryEvolver;
import cz.matfyz.evolution.querying.QueryUpdateResult;
import cz.matfyz.evolution.schema.SchemaCategoryUpdate;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.edit.InferenceEditor;
import cz.matfyz.inference.schemaconversion.Layout;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.server.Configuration.ServerProperties;
import cz.matfyz.server.Configuration.SparkProperties;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.payload.CategoryToModelPayload;
import cz.matfyz.server.entity.action.payload.ModelToCategoryPayload;
import cz.matfyz.server.entity.action.payload.RSDToCategoryPayload;
import cz.matfyz.server.entity.action.payload.UpdateSchemaPayload;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.evolution.SchemaUpdate;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.job.Run;
import cz.matfyz.server.entity.job.data.InferenceJobData;
import cz.matfyz.server.entity.job.data.ModelJobData;
import cz.matfyz.server.repository.LogicalModelRepository.LogicalModelWithDatasource;
import cz.matfyz.server.entity.logicalmodel.LogicalModelInit;
import cz.matfyz.server.entity.mapping.MappingInit;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.entity.query.QueryVersion;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.exception.SessionException;
import cz.matfyz.server.repository.JobRepository;
import cz.matfyz.server.repository.JobRepository.JobWithRun;
import cz.matfyz.server.repository.QueryRepository;
import cz.matfyz.server.repository.QueryRepository.QueryWithVersion;
import cz.matfyz.transformations.processes.DatabaseToInstance;
import cz.matfyz.transformations.processes.InstanceToDatabase;

import java.util.List;

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
    private MappingService mappingService;

    @Autowired
    private LogicalModelService logicalModelService;

    @Autowired
    private SchemaCategoryService schemaService;

    @Autowired
    private InstanceCategoryService instanceService;

    @Autowired
    private WrapperService wrapperService;

    @Autowired
    private ServerProperties server;

    @Autowired
    private SparkProperties spark;

    @Autowired
    private QueryRepository queryRepository;

    @Autowired
    private DatasourceService datasourceService;

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
            LOGGER.info("Job { id: {}, name: '{}' } is not ready.", job.id(), job.label);
            return;
        }

        job.state = Job.State.Running;
        repository.save(job);
        LOGGER.info("Job { id: {}, name: '{}' } started.", job.id(), job.label);

        try {
            processJobByType(run, job);
            LOGGER.info("Job { id: {}, name: '{}' } finished.", job.id(), job.label);
            if (job.payload instanceof RSDToCategoryPayload) {
                job.state = Job.State.Waiting;
            } else {
                job.state = Job.State.Finished;
            }
            repository.save(job);
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
        if (job.payload instanceof CategoryToModelPayload categoryToModelPayload)
            categoryToModelAlgorithm(run, job, categoryToModelPayload);
        else if (job.payload instanceof ModelToCategoryPayload modelToCategoryPayload)
            modelToCategoryAlgorithm(run, job, modelToCategoryPayload);
        else if (job.payload instanceof UpdateSchemaPayload updateSchemaPayload)
            updateSchemaAlgorithm(run, updateSchemaPayload);
        else if (job.payload instanceof RSDToCategoryPayload rsdToCategoryPayload)
            rsdToCategoryAlgorithm(run, job, rsdToCategoryPayload);

        //Thread.sleep(JOB_DELAY_IN_SECONDS * 1000);
    }


    private void modelToCategoryAlgorithm(Run run, Job job, ModelToCategoryPayload payload) {
        if (run.sessionId == null)
            throw SessionException.notFound(run.id());

        final DatasourceWrapper datasource = logicalModelService.find(payload.logicalModelId()).datasource();
        final AbstractPullWrapper pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();
        final List<MappingWrapper> mappingWrappers = mappingService.findAll(payload.logicalModelId());

        final SchemaCategory schema = schemaService.find(run.categoryId).toSchemaCategory();
        @Nullable InstanceCategory instance = instanceService.loadCategory(run.sessionId, schema);
        //System.out.println("jobexecutor: " + instance.objects());
        //System.out.println("print if non empty");
        System.out.println("instance before");
        System.out.println(instance);

        if (mappingWrappers.isEmpty())
            System.out.println("mapping wrappers is empty");

        for (final MappingWrapper mappingWrapper : mappingWrappers) {
            final Mapping mapping = mappingWrapper.toMapping(schema);
            instance = new DatabaseToInstance().input(mapping, instance, pullWrapper).run();
        }

        System.out.println("instance after");
        System.out.println(instance);

        if (instance != null)
            instanceService.saveCategory(run.sessionId, run.categoryId, instance);
    }

    private void categoryToModelAlgorithm(Run run, Job job, CategoryToModelPayload payload) {
        if (run.sessionId == null)
            throw SessionException.notFound(job.id());

        final SchemaCategory schema = schemaService.find(run.categoryId).toSchemaCategory();
        @Nullable InstanceCategory instance = instanceService.loadCategory(run.sessionId, schema);

        final DatasourceWrapper datasource = logicalModelService.find(payload.logicalModelId()).datasource();
        final List<Mapping> mappings = mappingService.findAll(payload.logicalModelId()).stream()
            .map(wrapper -> wrapper.toMapping(schema))
            .toList();

        final AbstractControlWrapper control = wrapperService.getControlWrapper(datasource);

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

            if (server.executeModels() && control.isWritable()) {
                LOGGER.info("Start executing models ...");
                control.execute(result.statements());
                LOGGER.info("... models executed.");
            }
            /*else { LOGGER.info("Models didn't get executed.");}*/
            /* for now I choose not to execute the statements, but just see if they even get created
            LOGGER.info("Start executing models ...");
            control.execute(result.statements());
            LOGGER.info("... models executed."); */
        }

        job.data = new ModelJobData(output.toString());
    }

    private void updateSchemaAlgorithm(Run run, UpdateSchemaPayload payload) {
        final List<QueryWithVersion> prevQueries = queryRepository.findAllInCategoryWithVersion(run.categoryId, payload.prevVersion());
        final List<QueryWithVersion> nextQueries = queryRepository.findAllInCategoryWithVersion(run.categoryId, payload.nextVersion());
        final List<QueryWithVersion> filteredPrevQueries = ArrayUtils.filterSorted(prevQueries, nextQueries);

        final QueryEvolver evolver = createQueryEvolver(run.categoryId, payload.prevVersion(), payload.nextVersion());

        for (final var query : filteredPrevQueries) {
            final QueryUpdateResult updateResult = evolver.run(query.version().content);
            final var newVersion = QueryVersion.createNew(
                query.query().id(),
                payload.nextVersion(),
                updateResult.nextContent,
                updateResult.errors
            );
            queryRepository.save(newVersion);
        }
    }

    private QueryEvolver createQueryEvolver(Id categoryId, Version prevVersion, Version nextVersion) {
        final SchemaCategoryWrapper wrapper = schemaService.find(categoryId);
        final List<SchemaCategoryUpdate> updates = schemaService
            .findAllUpdates(categoryId).stream()
            .filter(u -> u.prevVersion.compareTo(prevVersion) >= 0 && u.nextVersion.compareTo(nextVersion) <= 0)
            .map(SchemaUpdate::toEvolution).toList();

        final SchemaCategory prevCategory = wrapper.toSchemaCategory();
        final SchemaCategory nextCategory = wrapper.toSchemaCategory();
        SchemaCategoryUpdate.setToVersion(prevCategory, updates, wrapper.version, prevVersion);
        SchemaCategoryUpdate.setToVersion(nextCategory, updates, wrapper.version, nextVersion);

        return new QueryEvolver(prevCategory, nextCategory, updates);
    }

    private void rsdToCategoryAlgorithm(Run run, Job job, RSDToCategoryPayload payload) {
        // extracting the empty SK wrapper
        final DatasourceWrapper datasourceWrapper = datasourceService.find(payload.datasourceId());

        final var sparkSettings = new SparkSettings(spark.master(), spark.checkpoint());
        final AbstractInferenceWrapper inferenceWrapper = wrapperService.getControlWrapper(datasourceWrapper).getInferenceWrapper(sparkSettings);

        final List<CategoryMappingPair> categoryMappingPairs = new MMInferOneInAll()
            .input(inferenceWrapper, payload.kindName())
            .run();

        final var pair = CategoryMappingPair.merge(categoryMappingPairs);
        final SchemaCategory schema = pair.schema();
        final MetadataCategory metadata = pair.metadata();
        final List<Mapping> mappings = pair.mappings();


        Layout.applyToMetadata(schema, metadata);

        job.data = InferenceJobData.fromSchemaCategory(List.of(), schema, metadata, mappings);
    }

    public JobWithRun continueRSDToCategoryProcessing(JobWithRun job, InferenceJobData data, InferenceEdit edit, boolean isFinal) {
        final List<InferenceEdit> edits = data.edits();
        if (!isFinal) {
            if (edit == null)
                edits.remove(edits.size() - 1); // Cancel edit.
            else
                edits.add(edit); // New edit.
        }

        final var schema = SchemaSerializer.deserialize(data.schema());
        final var metadata = MetadataSerializer.deserialize(data.metadata(), schema);
        final var mappings = data.mappings().stream().map(s -> s.toMapping(schema)).toList();

        final InferenceEditor inferenceEditor = isFinal
            ? new InferenceEditor(schema, metadata, mappings, edits)
            : new InferenceEditor(schema, metadata, edits);

        inferenceEditor.applyEdits();

        job.job().data = InferenceJobData.fromSchemaCategory(edits, schema, metadata, mappings);

        if (isFinal)
            finishRSDToCategoryProcessing(job, schema, metadata, inferenceEditor.getFinalMapping());

        return job;
    }

    private void finishRSDToCategoryProcessing(JobWithRun job, SchemaCategory schema, MetadataCategory metadata, Mapping finalMapping) {
        final var wrapper = schemaService.find(job.run().categoryId);
        final var newWrapper = SchemaCategoryWrapper.fromSchemaCategory(wrapper.id(), wrapper.label, wrapper.version, wrapper.systemVersion, schema, metadata);
        schemaService.replace(newWrapper);

        final RSDToCategoryPayload payload = (RSDToCategoryPayload) job.job().payload;
        final DatasourceWrapper datasource = datasourceService.find(payload.datasourceId());
        final LogicalModelWithDatasource logicalModel = createLogicalModel(datasource.id(), newWrapper.id(), "Initial logical model");

        final MappingInit init = MappingInit.fromMapping(finalMapping, logicalModel.logicalModel().id());
        mappingService.createNew(init);
    }

    private LogicalModelWithDatasource createLogicalModel(Id datasourceId, Id categoryId, String initialLogicalModelLabel) {
        final LogicalModelInit logicalModelInit = new LogicalModelInit(datasourceId, categoryId, initialLogicalModelLabel);
        return logicalModelService.createNew(logicalModelInit);
    }
}
