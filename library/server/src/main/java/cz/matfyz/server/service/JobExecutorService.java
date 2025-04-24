package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
import cz.matfyz.abstractwrappers.BaseControlWrapper.DefaultControlWrapperProvider;
import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataObject.Position;
import cz.matfyz.core.metadata.MetadataSerializer;
import cz.matfyz.core.rsd.Candidates;
import cz.matfyz.core.rsd.CandidatesSerializer;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer;
import cz.matfyz.core.utils.ArrayUtils;
import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.category.SchemaEvolutionAlgorithm;
import cz.matfyz.evolution.querying.QueryEvolver;
import cz.matfyz.evolution.querying.QueryEvolutionResult;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.edit.InferenceEditor;
import cz.matfyz.inference.schemaconversion.Layout;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingsPair;
import cz.matfyz.inference.schemaconversion.utils.InferenceResult;
import cz.matfyz.inference.schemaconversion.utils.LayoutType;
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
import cz.matfyz.server.entity.file.File;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.job.Run;
import cz.matfyz.server.entity.job.data.InferenceJobData;
import cz.matfyz.server.entity.job.data.ModelJobData;
import cz.matfyz.server.entity.mapping.MappingInit;
import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.exception.SessionException;
import cz.matfyz.server.global.Configuration.ServerProperties;
import cz.matfyz.server.global.Configuration.SparkProperties;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.repository.EvolutionRepository;
import cz.matfyz.server.repository.InstanceCategoryRepository;
import cz.matfyz.server.repository.JobRepository;
import cz.matfyz.server.repository.MappingRepository;
import cz.matfyz.server.repository.JobRepository.JobInfo;
import cz.matfyz.server.repository.JobRepository.JobWithRun;
import cz.matfyz.server.repository.QueryRepository;
import cz.matfyz.server.repository.SchemaCategoryRepository;
import cz.matfyz.transformations.processes.DatabaseToInstance;
import cz.matfyz.transformations.processes.InstanceToDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    private JobService service;

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private MappingService mappingService;

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

    @Autowired
    private FileService fileService;

    // The jobs in general can not run in parallel (for example, one can export from the instance category the second one is importing into).
    // There is an opportunity for optimalizaiton (only importing / only exporting jobs can run in parallel) but it would require synchronization on the instance level in the transformation algorithms.

    // This method will be executed 2 seconds after the previous execution finished.
    @Scheduled(fixedDelay = 2000)
    public void processAllRuns() {
        final var activeRunIds = repository.findAllActiveRunIds();
        for (final var runId : activeRunIds)
            processRun(runId);
    }

    private void processRun(Id runId) {
        final var run = repository.findRunWithJobs(runId);
        final @Nullable JobInfo nextJob = service.getNextReadyJob(run.jobs());
        if (nextJob != null) {
            executeJob(nextJob.id());
            return;
        }

        // The job can't be executed, so we mark the run as inactive. As soon as any job in the run changes, the run will be marked as active again.
        repository.deactivateRun(runId);
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
            throw SessionException.runNotInSession(run.id());

        final SchemaCategory schema = schemaRepository.find(run.categoryId).toSchemaCategory();
        final @Nullable InstanceCategoryWrapper instanceWrapper = instanceRepository.find(run.sessionId);

        InstanceCategory instance = instanceWrapper != null
            ? instanceWrapper.toInstanceCategory(schema)
            : new InstanceBuilder(schema).build();

        final DatasourceWrapper datasourceWrapper = datasourceRepository.find(payload.datasourceId());
        final Datasource datasource = datasourceWrapper.toDatasource();
        final List<Mapping> mappings = mappingRepository.findAllInCategory(run.categoryId, payload.datasourceId()).stream()
            .filter(wrapper -> payload.mappingIds().isEmpty() || payload.mappingIds().contains(wrapper.id()))
            .map(wrapper -> wrapper.toMapping(datasource, schema))
            .toList();

        final AbstractPullWrapper pullWrapper = wrapperService.getControlWrapper(datasourceWrapper).getPullWrapper();

        for (final Mapping mapping : mappings)
            instance = new DatabaseToInstance().input(mapping, instance, pullWrapper).run();

        if (instance != null)
            instanceService.saveCategory(run.sessionId, run.categoryId, instance);
    }

    private void categoryToModelAlgorithm(Run run, Job job, CategoryToModelPayload payload) {
        if (run.sessionId == null)
            throw SessionException.runNotInSession(run.id());

        final SchemaCategory schema = schemaRepository.find(run.categoryId).toSchemaCategory();
        final @Nullable InstanceCategoryWrapper instanceWrapper = instanceRepository.find(run.sessionId);

        final InstanceCategory instance = instanceWrapper != null
            ? instanceWrapper.toInstanceCategory(schema)
            : new InstanceBuilder(schema).build();

        final DatasourceWrapper datasourceWrapper = datasourceRepository.find(payload.datasourceId());
        final Datasource datasource = datasourceWrapper.toDatasource();
        final List<Mapping> mappings = mappingRepository.findAllInCategory(run.categoryId, payload.datasourceId()).stream()
            .filter(wrapper -> payload.mappingIds().isEmpty() || payload.mappingIds().contains(wrapper.id()))
            .map(wrapper -> wrapper.toMapping(datasource, schema))
            .toList();

        final AbstractControlWrapper control = wrapperService.getControlWrapper(datasourceWrapper);

        final var result = new InstanceToDatabase()
            .input(
                mappings,
                instance,
                control.getDDLWrapper(),
                control.getDMLWrapper(),
                control.getICWrapper()
            )
            .run();

        // TODO - find a better way how to execute the changes (currently its too likely to fail)

        // TODO - verzovat databáze - tj. vytvořit vždy novou databázi (v rámci stejného engine)
        //  - např. uživatel zvolí "my_db", tak vytvářet "my_db_1", "my_db_2" a podobně
        //  - resp. při opětovném spuštění to smazat a vytvořit znovu ...

        Boolean executed = false;

        if (server.executeModels() && control.isWritable()) {
            LOGGER.info("Start executing models ...");
            control.execute(result.statements());
            LOGGER.info("... models executed.");
            executed = true;
        }

        final var resultString = result.statementsAsString();

        final File file = fileService.create(job.id(), datasourceWrapper.id(), run.categoryId, run.label, executed, datasource.type, resultString);

        // Instead of the result we are saving only the id of the file, where the result is saved
        job.data = new ModelJobData(file.id().toString());
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

        final SchemaCategory prevSchema = wrapper.toSchemaCategory();
        final MetadataCategory prevMetadata = wrapper.toMetadataCategory(prevSchema);
        final SchemaCategory nextSchema = wrapper.toSchemaCategory();
        final MetadataCategory nextMetadata = wrapper.toMetadataCategory(nextSchema);
        SchemaEvolutionAlgorithm.setToVersion(prevSchema, prevMetadata, updates, wrapper.version(), prevVersion);
        SchemaEvolutionAlgorithm.setToVersion(nextSchema, nextMetadata, updates, wrapper.version(), nextVersion);

        return new QueryEvolver(prevSchema, nextSchema, updates);
    }

    private void rsdToCategoryAlgorithm(Run run, Job job, RSDToCategoryPayload payload) {
        final var sparkSettings = new SparkSettings(spark.master(), spark.checkpoint());

        final List<Datasource> datasources = new ArrayList<>();
        final var provider = new DefaultControlWrapperProvider();
        payload.datasourceIds().forEach(id -> {
            final var datasourceWrapper = datasourceRepository.find(id);
            final var datasource = datasourceWrapper.toDatasource();
            final var control = wrapperService.getControlWrapper(datasourceWrapper).enableSpark(sparkSettings);
            datasources.add(datasource);
            provider.setControlWrapper(datasource, control);
        });

        final InferenceResult inferenceResult = new MMInferOneInAll()
            .input(provider)
            .run();

        final Candidates candidates = inferenceResult.candidates();
        final List<CategoryMappingsPair> categoryMappingPairs = inferenceResult.pairs();

        final var pair = CategoryMappingsPair.merge(categoryMappingPairs);
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

    public JobWithRun continueRSDToCategoryProcessing(JobWithRun job, InferenceJobData data, @Nullable InferenceEdit edit, boolean isFinal, @Nullable LayoutType layoutType, @Nullable Map<Key, Position> positionsMap) {
        final var schema = SchemaSerializer.deserialize(data.inferenceSchema());
        final var metadata = MetadataSerializer.deserialize(data.inferenceMetadata(), schema);
        final var candidates = CandidatesSerializer.deserialize(data.candidates());

        final var mappings = data.datasources().stream().flatMap(serializedDatasource -> {
            final var datasourceWrapper = datasourceRepository.find(new Id(serializedDatasource.datasourceId()));
            final var datasource = datasourceWrapper.toDatasource();
            return serializedDatasource.mappings().stream().map(serializedMapping -> serializedMapping.toMapping(datasource, schema));
        }).toList();

        List<InferenceEdit> edits = data.edits();

        if (layoutType != null)
            Layout.applyToMetadata(schema, metadata, layoutType);
        else if (positionsMap != null)
            Layout.updatePositions(schema, metadata, positionsMap);
        else
            edits = updateInferenceEdits(edits, edit, isFinal);

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
    private List<InferenceEdit> updateInferenceEdits(List<InferenceEdit> edits, InferenceEdit edit, boolean isFinal) {
        if (edit == null) {
            return isFinal || edits.isEmpty()
                ? edits
                : edits.subList(0, edits.size() - 1);
        }

        final Integer editIndex = findEditIdx(edit, edits);
        if (editIndex == null) {
            edit.setId(edits.size());
            final var output = new ArrayList<>(edits);
            output.add(edit);

            return output;
        }

        InferenceEdit existingEdit = edits.get(editIndex);
        existingEdit.setActive(!existingEdit.isActive());

        return edits;
    }

    private Integer findEditIdx(InferenceEdit edit, List<InferenceEdit> manual) {
        for (int i = 0; i < manual.size(); i++)
            if (manual.get(i).getId().equals(edit.getId()))
                return i;

        return null;
    }

    private void finishRSDToCategoryProcessing(JobWithRun job, SchemaCategory schema, MetadataCategory metadata, List<Mapping> mappings) {
        final var categoryWrapper = schemaRepository.find(job.run().categoryId);

        final var version = categoryWrapper.systemVersion().generateNext();
        categoryWrapper.update(version, schema, metadata);
        schemaRepository.save(categoryWrapper);

        final RSDToCategoryPayload payload = (RSDToCategoryPayload) job.job().payload;
        final Map<Id, DatasourceWrapper> datasourceWrappers = new TreeMap<>();
        payload.datasourceIds().stream().map(datasourceRepository::find).forEach(dw -> datasourceWrappers.put(dw.id(), dw));

        for (final Mapping mapping : mappings) {
            final MappingInit init = MappingInit.fromMapping(mapping, categoryWrapper.id(), new Id(mapping.datasource().identifier));
            mappingService.create(init);
        }

        job.job().state = Job.State.Finished;
    }

}
