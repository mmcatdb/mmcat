package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceCategoryBuilder;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer;
import cz.matfyz.core.rsd.Candidates;
import cz.matfyz.core.rsd.CandidatesSerializer;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer;
import cz.matfyz.core.utils.ArrayUtils;
import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.querying.QueryEvolver;
import cz.matfyz.evolution.querying.QueryEvolutionResult;
import cz.matfyz.evolution.schema.SchemaEvolutionAlgorithm;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.edit.InferenceEditSerializer;
import cz.matfyz.inference.edit.InferenceEditor;
import cz.matfyz.inference.schemaconversion.Layout;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.inference.schemaconversion.utils.InferenceResult;
import cz.matfyz.inference.schemaconversion.utils.LayoutType;
import cz.matfyz.server.Configuration.ServerProperties;
import cz.matfyz.server.Configuration.SparkProperties;
import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.InstanceCategoryWrapper;
import cz.matfyz.server.entity.Query;
import cz.matfyz.server.entity.action.payload.CategoryToModelPayload;
import cz.matfyz.server.entity.action.payload.ModelToCategoryPayload;
import cz.matfyz.server.entity.action.payload.RSDToCategoryPayload;
import cz.matfyz.server.entity.action.payload.UpdateSchemaPayload;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.evolution.QueryEvolution;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.job.Run;
import cz.matfyz.server.entity.job.data.InferenceJobData;
import cz.matfyz.server.entity.job.data.ModelJobData;
import cz.matfyz.server.entity.mapping.MappingInit;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.exception.SessionException;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.repository.EvolutionRepository;
import cz.matfyz.server.repository.InstanceCategoryRepository;
import cz.matfyz.server.repository.JobRepository;
import cz.matfyz.server.repository.LogicalModelRepository;
import cz.matfyz.server.repository.MappingRepository;
import cz.matfyz.server.repository.JobRepository.JobWithRun;
import cz.matfyz.server.repository.QueryRepository;
import cz.matfyz.server.repository.SchemaCategoryRepository;
import cz.matfyz.transformations.processes.DatabaseToInstance;
import cz.matfyz.transformations.processes.InstanceToDatabase;

import java.util.List;
import java.util.stream.Collectors;

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
    private MappingRepository mappingRepository;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private LogicalModelRepository logicalModelRepository;

    @Autowired
    private LogicalModelService logicalModelService;

    @Autowired
    private EvolutionRepository evolutionRepository;

    @Autowired
    private SchemaCategoryRepository schemaRepository;

    @Autowired
    private InstanceCategoryRepository instanceRepository;

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
    private DatasourceRepository datasourceRepository;

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

        final DatasourceWrapper datasource = logicalModelRepository.find(payload.logicalModelId()).datasource();
        final AbstractPullWrapper pullWrapper = wrapperService.getControlWrapper(datasource).getPullWrapper();
        // TODO
        if (true)
            throw new UnsupportedOperationException("NOT LOGICAL MODEL ID ...");
        final List<MappingWrapper> mappingWrappers = mappingRepository.findAll(payload.logicalModelId());

        final SchemaCategory schema = schemaRepository.find(run.categoryId).toSchemaCategory();
        final @Nullable InstanceCategoryWrapper instanceWrapper = instanceRepository.find(run.sessionId);

        InstanceCategory instance = instanceWrapper != null
            ? instanceWrapper.toInstanceCategory(schema)
            : new InstanceCategoryBuilder().setSchemaCategory(schema).build();

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

        final SchemaCategory schema = schemaRepository.find(run.categoryId).toSchemaCategory();
        final @Nullable InstanceCategoryWrapper instanceWrapper = instanceRepository.find(run.sessionId);

        final InstanceCategory instance = instanceWrapper != null
            ? instanceWrapper.toInstanceCategory(schema)
            : new InstanceCategoryBuilder().setSchemaCategory(schema).build();

        final DatasourceWrapper datasource = logicalModelRepository.find(payload.logicalModelId()).datasource();
        // TODO
        if (true)
        throw new UnsupportedOperationException("NOT LOGICAL MODEL ID ...");
        final List<Mapping> mappings = mappingRepository.findAll(payload.logicalModelId()).stream()
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
        System.out.println(output.toString());
        job.data = new ModelJobData(output.toString());
    }

    private void updateSchemaAlgorithm(Run run, UpdateSchemaPayload payload) {
        // FIXME filter correctly by versions.
        final List<Query> prevQueries = queryRepository.findAllInCategory(run.categoryId, payload.prevVersion());
        final List<Query> nextQueries = queryRepository.findAllInCategory(run.categoryId, payload.nextVersion());

        // Some high order magic right here because Java generics suck ass!
        @SuppressWarnings("unchecked")
        final List<Query> filteredPrevQueries = (List<Query>) (Object) ArrayUtils.filterSorted((List<Entity>) (Object) prevQueries, (List<Entity>) (Object) nextQueries);

        final QueryEvolver evolver = createQueryEvolver(run.categoryId, payload.prevVersion(), payload.nextVersion());

        // FIXME doesn't do anything now.
        // for (final var query : filteredPrevQueries) {
        //     final QueryEvolutionResult updateResult = evolver.run(query.content);
        //     final var newVersion = QueryEvolution.createNew(
        //         run.categoryId,
        //         // FIXME get system version
        //         payload.nextVersion(),
        //         query.id(),
        //         updateResult.nextContent,
        //         updateResult.errors
        //     );
        //     evolutionRepository.create(newVersion);
        // }
    }

    private QueryEvolver createQueryEvolver(Id categoryId, Version prevVersion, Version nextVersion) {
        final SchemaCategoryWrapper wrapper = schemaRepository.find(categoryId);
        final List<SchemaEvolutionAlgorithm> updates = evolutionRepository
            .findAllSchemaEvolutions(categoryId).stream()
            // TODO Check if the version comparison is correct (with respect to the previous algorithm)
            .filter(u -> u.version.compareTo(prevVersion) > 0 && u.version.compareTo(nextVersion) <= 0)
            // .filter(u -> u.prevVersion.compareTo(prevVersion) >= 0 && u.nextVersion.compareTo(nextVersion) <= 0)
            .map(u -> u.toSchemaAlgorithm(prevVersion)).toList();
            // .map(SchemaUpdate::toEvolution).toList();

        final SchemaCategory prevCategory = wrapper.toSchemaCategory();
        final SchemaCategory nextCategory = wrapper.toSchemaCategory();
        SchemaEvolutionAlgorithm.setToVersion(prevCategory, updates, wrapper.version(), prevVersion);
        SchemaEvolutionAlgorithm.setToVersion(nextCategory, updates, wrapper.version(), nextVersion);

        return new QueryEvolver(prevCategory, nextCategory, updates);
    }

    private void rsdToCategoryAlgorithm(Run run, Job job, RSDToCategoryPayload payload) {
        // extracting the empty SK wrapper
        final DatasourceWrapper datasourceWrapper = datasourceRepository.find(payload.datasourceId());

        final var sparkSettings = new SparkSettings(spark.master(), spark.checkpoint());
        final AbstractInferenceWrapper inferenceWrapper = wrapperService.getControlWrapper(datasourceWrapper).getInferenceWrapper(sparkSettings);

        final InferenceResult inferenceResult = new MMInferOneInAll()
            .input(inferenceWrapper)
            .run();

        final Candidates candidates = inferenceResult.candidates();
        final List<CategoryMappingPair> categoryMappingPairs = inferenceResult.pairs();

        final var pair = CategoryMappingPair.merge(categoryMappingPairs);
        final SchemaCategory schema = pair.schema();
        final MetadataCategory metadata = pair.metadata();
        final List<Mapping> mappings = pair.mappings();

        Layout.applyToMetadata(schema, metadata, LayoutType.FR);

        job.data = InferenceJobData.fromSchemaCategory(
            List.of(),
            schema,
            schema,
            metadata,
            metadata,
            LayoutType.FR,
            candidates,
            mappings
        );
    }

    public JobWithRun continueRSDToCategoryProcessing(JobWithRun job, InferenceJobData data, InferenceEdit edit, boolean isFinal, LayoutType layoutType) {
        final var schema = SchemaSerializer.deserialize(data.inferenceSchema());
        final var metadata = MetadataSerializer.deserialize(data.inferenceMetadata(), schema);
        final var candidates = CandidatesSerializer.deserialize(data.candidates());
        final var mappings = data.mappings().stream().map(s -> s.toMapping(schema)).toList();

        final List<InferenceEdit> edits = data.edits().stream()
            .map(InferenceEditSerializer::deserialize)
            .collect(Collectors.toList());

        if (layoutType != null) {
            Layout.applyToMetadata(schema, metadata, layoutType);
        } else {
            updateInferenceEdits(edits, edit, isFinal);
        }

        final InferenceEditor inferenceEditor = isFinal
            ? new InferenceEditor(schema, metadata, mappings, edits)
            : new InferenceEditor(schema, metadata, edits);

        inferenceEditor.applyEdits();

        job.job().data = InferenceJobData.fromSchemaCategory(
            edits,
            schema,
            inferenceEditor.getSchemaCategory(),
            metadata,
            inferenceEditor.getMetadata(),
            layoutType != null ? layoutType : data.layoutType(),
            candidates,
            mappings
        );

        if (isFinal)
            finishRSDToCategoryProcessing(job, inferenceEditor.getSchemaCategory(), inferenceEditor.getMetadata(), inferenceEditor.getMappings());

        return job;
    }

    // TODO: move this to an util class - should I?
    private void updateInferenceEdits(List<InferenceEdit> edits, InferenceEdit edit, boolean isFinal) {
        if (edit == null) {
            if (!isFinal && !edits.isEmpty()) {
                edits.remove(edits.size() - 1);
            }
            return;
        }
        Integer editIdx = findEditIdx(edit, edits);
        if (editIdx == null) {
            edit.setId(edits.size());
            edits.add(edit);
        } else {
            InferenceEdit existingEdit = edits.get(editIdx);
            existingEdit.setActive(!existingEdit.isActive());
        }
    }

    private Integer findEditIdx(InferenceEdit edit, List<InferenceEdit> manual) {
        for (int i = 0; i < manual.size(); i++) {
            if (manual.get(i).getId().equals(edit.getId())) {
                return i;
            }
        }
        return null;
    }

    private void finishRSDToCategoryProcessing(JobWithRun job, SchemaCategory schema, MetadataCategory metadata, List<Mapping> mappings) {
        final var wrapper = schemaRepository.find(job.run().categoryId);

        final var version = wrapper.systemVersion().generateNext();
        wrapper.update(version, schema, metadata);
        schemaRepository.save(wrapper);

        final RSDToCategoryPayload payload = (RSDToCategoryPayload) job.job().payload;
        final DatasourceWrapper datasource = datasourceRepository.find(payload.datasourceId());
        logicalModelService.create(wrapper.id(), datasource.id(), "Initial logical model");

        for (final Mapping mapping : mappings) {
            final MappingInit init = MappingInit.fromMapping(mapping, wrapper.id(), datasource.id());
            mappingService.create(init);
        }
    }

}
