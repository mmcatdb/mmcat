package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.utils.ArrayUtils;
import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.querying.QueryEvolver;
import cz.matfyz.evolution.querying.QueryUpdateResult;
import cz.matfyz.evolution.schema.SchemaCategoryUpdate;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.inference.edit.AbstractInferenceEdit;
import cz.matfyz.inference.edit.InferenceEditor;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.server.builder.MetadataContext;
import cz.matfyz.server.builder.GeneratedDataModel;
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
import cz.matfyz.server.repository.LogicalModelRepository.LogicalModelWithDatasource;
import cz.matfyz.server.repository.MappingRepository.MappingJsonValue;
import cz.matfyz.server.entity.logicalmodel.LogicalModelInit;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.entity.query.QueryVersion;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper.Position;
import cz.matfyz.server.exception.SessionException;
import cz.matfyz.server.repository.JobRepository;
import cz.matfyz.server.repository.JobRepository.JobWithRun;
import cz.matfyz.server.repository.QueryRepository;
import cz.matfyz.server.repository.QueryRepository.QueryWithVersion;
import cz.matfyz.server.utils.InferenceJobData;
import cz.matfyz.server.utils.LayoutUtil;
import cz.matfyz.server.utils.SchemaCategoryUtil;
import cz.matfyz.transformations.processes.DatabaseToInstance;
import cz.matfyz.transformations.processes.InstanceToDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
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
            LOGGER.info("Job { id: {}, name: '{}' } is not ready.", job.id, job.label);
            return;
        }

        job.state = Job.State.Running;
        repository.save(job);
        LOGGER.info("Job { id: {}, name: '{}' } started.", job.id, job.label);

        try {
            processJobByType(run, job);
            LOGGER.info("Job { id: {}, name: '{}' } finished.", job.id, job.label);
            if (job.payload instanceof RSDToCategoryPayload) {
                job.state = Job.State.Waiting;
            } else {
                job.state = Job.State.Finished;
            }
            repository.save(job);
        }
        catch (Exception e) {
            final NamedException finalException = e instanceof NamedException namedException ? namedException : new OtherException(e);

            LOGGER.error(String.format("Job { id: %s, name: '%s' } failed.", job.id, job.label), finalException);
            job.state = Job.State.Failed;
            job.data = finalException.toSerializedException();
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
            throw SessionException.notFound(run.id);

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
            throw SessionException.notFound(job.id);

        final SchemaCategory schema = schemaService.find(run.categoryId).toSchemaCategory();
        @Nullable InstanceCategory instance = instanceService.loadCategory(run.sessionId, schema);

        final DatasourceWrapper datasource = logicalModelService.find(payload.logicalModelId()).datasource();
        final List<Mapping> mappings = mappingService.findAll(payload.logicalModelId()).stream()
            .map(wrapper -> wrapper.toMapping(schema))
            .toList();

        final AbstractControlWrapper control = wrapperService.getControlWrapper(datasource);

        GeneratedDataModel generatedDataModel = new GeneratedDataModel(schema.label, run.categoryId, datasource.type);

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

            // Adding the schema for each mapping here
            generatedDataModel.addAccessPath(mapping.accessPath());
        }

        job.generatedDataModel = generatedDataModel.toString();
        job.data = output.toString();
    }

    private void updateSchemaAlgorithm(Run run, UpdateSchemaPayload payload) {
        final List<QueryWithVersion> prevQueries = queryRepository.findAllInCategoryWithVersion(run.categoryId, payload.prevVersion());
        final List<QueryWithVersion> nextQueries = queryRepository.findAllInCategoryWithVersion(run.categoryId, payload.nextVersion());
        final List<QueryWithVersion> filteredPrevQueries = ArrayUtils.filterSorted(prevQueries, nextQueries);

        final QueryEvolver evolver = createQueryEvolver(run.categoryId, payload.prevVersion(), payload.nextVersion());

        for (final var query : filteredPrevQueries) {
            final QueryUpdateResult updateResult = evolver.run(query.version().content);
            final var newVersion = QueryVersion.createNew(
                query.query().id,
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
        final SchemaCategoryWrapper originalSchemaWrapper = schemaService.find(run.categoryId);
        final DatasourceWrapper datasourceWrapper = datasourceService.find(payload.datasourceId());

        final var sparkSettings = new SparkSettings(spark.master(), spark.checkpoint());
        final AbstractInferenceWrapper inferenceWrapper = wrapperService.getControlWrapper(datasourceWrapper).getInferenceWrapper(sparkSettings);

        final List<CategoryMappingPair> categoryMappingPairs = new MMInferOneInAll()
            .input(inferenceWrapper, payload.kindName(), originalSchemaWrapper.label)
            .run();

        SchemaCategory schemaCategory = CategoryMappingPair.mergeCategory(categoryMappingPairs, originalSchemaWrapper.label);
        List<Mapping> mappings = CategoryMappingPair.getMappings(categoryMappingPairs);

        final SchemaCategoryWrapper schemaWrapper = SchemaCategoryUtil.createWrapperFromCategory(schemaCategory);
        final List<MappingJsonValue> mappingsJsonValue = createMappingsJsonValue(mappings);

        InferenceJobData inferenceJobData = new InferenceJobData(new InferenceJobData.InferenceData(schemaWrapper, mappingsJsonValue));
        try {
            job.data = inferenceJobData.toJsonValue();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process JSON while saving SK and mapping to job.data", e);
        }
    }

    public JobWithRun continueRSDToCategoryProcessing(JobWithRun job, InferenceJobData inferenceJobData, AbstractInferenceEdit edit, boolean savePermanent) {
        if (edit == null && !savePermanent) { //cancel edit
            inferenceJobData.manual.remove(inferenceJobData.manual.size() - 1);
        } else if (!savePermanent) { // new edit
            inferenceJobData.manual.add(edit);
        }
        SchemaCategory inferenceSchemaCategory = inferenceJobData.inference.schemaCategory.toSchemaCategory();
        List<MappingJsonValue> mappingsJsonValue = inferenceJobData.inference.mapping;

        InferenceEditor inferenceEditor = savePermanent ? new InferenceEditor(inferenceSchemaCategory, createMappings(mappingsJsonValue, inferenceSchemaCategory), inferenceJobData.manual) : new InferenceEditor(inferenceJobData.inference.schemaCategory.toSchemaCategory(), inferenceJobData.manual);

        inferenceJobData.finalSchema = makeEdits(inferenceEditor);

        if (savePermanent) {
            finishRSDToCategoryProcessing(job, inferenceJobData.finalSchema, inferenceEditor.getMappings());
        } else {
            try {
                job.job().data = inferenceJobData.toJsonValue();
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to process JSON while saving SK and mapping to job.data", e);
            }
        }
        return job;
    }

    public void finishRSDToCategoryProcessing(JobWithRun jobWithRun, SchemaCategoryWrapper finalSchema, List<Mapping> finalMappings) {
        RSDToCategoryPayload payload = (RSDToCategoryPayload) jobWithRun.job().payload;

        final DatasourceWrapper datasourceWrapper = datasourceService.find(payload.datasourceId());
        final LogicalModelWithDatasource logicalModelWithDatasource = createLogicalModel(datasourceWrapper.id, jobWithRun.run().categoryId, "Initial logical model");

        schemaService.overwriteInfo(finalSchema, jobWithRun.run().categoryId);

        for (Mapping mapping : finalMappings) {
            mappingService.createNew(mapping, logicalModelWithDatasource.logicalModel().id);
        }
    }

    private List<Mapping> createMappings(List<MappingJsonValue> mappingsJsonValue, SchemaCategory schemaCategory) {
        List<Mapping> mappings = new ArrayList<>();

        for (MappingJsonValue mappingJsonValue : mappingsJsonValue) {
            Collection<Signature> primaryKey = new ArrayList<>();
            Collections.addAll(primaryKey, mappingJsonValue.primaryKey());
            mappings.add(new Mapping(
                                schemaCategory,
                                mappingJsonValue.rootObjectKey(),
                                mappingJsonValue.kindName(),
                                mappingJsonValue.accessPath(),
                                primaryKey
                        ));
        }
        return mappings;
    }

    private List<MappingJsonValue> createMappingsJsonValue(List<Mapping> mappings) {
        List<MappingJsonValue> mappingsJsonValue = new ArrayList<>();
        for (Mapping mapping : mappings) {
            mappingsJsonValue.add(new MappingJsonValue(
                                        mapping.rootObject().key(),
                                        mapping.primaryKey().toArray(new Signature[0]),
                                        mapping.kindName(),
                                        mapping.accessPath(),
                                        Version.generateInitial(),
                                        Version.generateInitial()
                                    ));

        }
        return mappingsJsonValue;
    }

    private SchemaCategoryWrapper makeEdits(InferenceEditor inferenceEditor) {
        inferenceEditor.applyEdits();
        return SchemaCategoryUtil.createWrapperFromCategory(inferenceEditor.getSchemaCategory());
    }

    private LogicalModelWithDatasource createLogicalModel(Id datasourceId, Id schemaCategoryId, String initialLogicalModelLabel) {
        LogicalModelInit logicalModelInit = new LogicalModelInit(datasourceId, schemaCategoryId, initialLogicalModelLabel);
        return logicalModelService.createNew(logicalModelInit);
    }
}
