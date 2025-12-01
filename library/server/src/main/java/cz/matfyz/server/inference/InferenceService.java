package cz.matfyz.server.inference;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
import cz.matfyz.abstractwrappers.BaseControlWrapper.DefaultControlWrapperProvider;
import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataObjex.Position;
import cz.matfyz.core.metadata.MetadataSerializer;
import cz.matfyz.core.rsd.Candidates;
import cz.matfyz.core.rsd.CandidatesSerializer;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.edit.InferenceEditor;
import cz.matfyz.inference.schemaconversion.Layout;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingsPair;
import cz.matfyz.inference.schemaconversion.utils.InferenceResult;
import cz.matfyz.inference.schemaconversion.utils.LayoutType;
import cz.matfyz.server.category.SchemaCategoryRepository;
import cz.matfyz.server.datasource.DatasourceEntity;
import cz.matfyz.server.datasource.DatasourceRepository;
import cz.matfyz.server.datasource.WrapperService;
import cz.matfyz.server.inference.InferenceWorkflowData.InferenceWorkflowStep;
import cz.matfyz.server.instance.CategoryToModelPayload;
import cz.matfyz.server.instance.ModelToCategoryPayload;
import cz.matfyz.server.job.Job;
import cz.matfyz.server.job.JobPayload;
import cz.matfyz.server.job.JobRepository;
import cz.matfyz.server.job.JobRepository.JobWithRun;
import cz.matfyz.server.job.JobService;
import cz.matfyz.server.mapping.MappingController.MappingInit;
import cz.matfyz.server.mapping.MappingRepository;
import cz.matfyz.server.mapping.MappingService;
import cz.matfyz.server.utils.Configuration.SparkProperties;
import cz.matfyz.server.utils.entity.Id;
import cz.matfyz.server.workflow.Workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InferenceService {

    @Autowired
    private WrapperService wrapperService;

    @Autowired
    private SparkProperties spark;

    @Autowired
    private DatasourceRepository datasourceRepository;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private SchemaCategoryRepository schemaRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobService jobService;

    @Autowired
    private MappingRepository mappingRepository;

    // #region Job

    public void startJob(Job job, InferencePayload payload) {
        final var sparkSettings = new SparkSettings(spark.master(), spark.checkpoint());

        final List<Datasource> datasources = new ArrayList<>();
        final var provider = new DefaultControlWrapperProvider();
        payload.datasourceIds().forEach(id -> {
            final var datasourceEntity = datasourceRepository.find(id);
            final var datasource = datasourceEntity.toDatasource();
            final var control = wrapperService.getControlWrapper(datasourceEntity).enableSpark(sparkSettings);
            datasources.add(datasource);
            provider.setControlWrapper(datasource, control);
        });

        final InferenceResult inferenceResult = new MMInferOneInAll()
            .input(provider)
            .run();

        final Candidates candidates = inferenceResult.candidates();
        final List<CategoryMappingsPair> categoryMappingPairs = inferenceResult.pairs();

        final var pair = CategoryMappingsPair.merge(categoryMappingPairs);
        Layout.applyToMetadata(pair.schema(), pair.metadata(), LayoutType.FR);

        job.data = InferenceJobData.fromSchemaCategory(
            List.of(),
            pair.schema(),
            pair.schema(),
            pair.metadata(),
            pair.metadata(),
            LayoutType.FR,
            candidates,
            pair.mappings()
        );
    }

    public JobWithRun updateJob(JobWithRun job, InferenceJobData data, @Nullable InferenceEdit edit, boolean isFinal, @Nullable LayoutType layoutType, @Nullable Map<Key, Position> positionsMap) {
        final var schema = SchemaSerializer.deserialize(data.inferenceSchema());
        final var metadata = MetadataSerializer.deserialize(data.inferenceMetadata(), schema);
        final var candidates = CandidatesSerializer.deserialize(data.candidates());

        final var mappings = data.datasources().stream().flatMap(serializedDatasource -> {
            final var datasourceEntity = datasourceRepository.find(new Id(serializedDatasource.datasourceId()));
            final var datasource = datasourceEntity.toDatasource();
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
            finishJob(job, inferenceEditor.getSchemaCategory(), inferenceEditor.getMetadata(), inferenceEditor.getMappings());

        return job;
    }

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

    private void finishJob(JobWithRun job, SchemaCategory schema, MetadataCategory metadata, List<Mapping> mappings) {
        final var categoryEntity = schemaRepository.find(job.run().categoryId);

        final var version = categoryEntity.systemVersion().generateNext();
        categoryEntity.update(version, schema, metadata);
        schemaRepository.save(categoryEntity);

        final InferencePayload payload = (InferencePayload) job.job().payload;
        final Map<Id, DatasourceEntity> datasourceEntities = new TreeMap<>();
        payload.datasourceIds().stream().map(datasourceRepository::find).forEach(dw -> datasourceEntities.put(dw.id(), dw));

        for (final Mapping mapping : mappings) {
            final MappingInit init = MappingInit.fromMapping(mapping, categoryEntity.id(), new Id(mapping.datasource().identifier));
            mappingService.create(init);
        }

        job.job().state = Job.State.Finished;
    }

    // #endregion
    // #region Workflow

    public Workflow continueWorkflow(Workflow workflow, InferenceWorkflowData data) {
        switch (data.step()) {
            case selectInputs -> continueSelectInputs(workflow, data);
            case editCategory -> continueEditCategory(workflow, data);
            case addMappings -> continueAddMappings(workflow, data);
            case finish -> throw new IllegalArgumentException("The workflow is already finished.");
            default -> throw new IllegalArgumentException("Unknown inference workflow step.");
        }

        return workflow;
    }

    private void continueSelectInputs(Workflow workflow, InferenceWorkflowData data) {
        // The user has to select the input datasource. Then we can create the inference job and the user can continue.
        if (data.inputDatasourceIds().isEmpty())
            throw new IllegalArgumentException("Input datasource is required.");

        final var inferenceJobId = jobService
            .createRun(workflow.categoryId, "Schema inference", List.of(new InferencePayload(data.inputDatasourceIds())))
            .jobs().get(0).id();

        final var newData = new InferenceWorkflowData(
            InferenceWorkflowStep.editCategory,
            data.inputDatasourceIds(),
            inferenceJobId,
            data.inputMappingIds()
        );

        workflow.jobId = inferenceJobId;
        workflow.data = newData;
    }

    private void continueEditCategory(Workflow workflow, InferenceWorkflowData data) {
        // The user has to wait for the job first. Then he can check the result - it probably needs some manual adjustments. After the user marks the job as finished, he can continue.
        final Job currentJob = jobRepository.find(data.inferenceJobId()).job();
        if (currentJob.state != Job.State.Finished)
            throw new IllegalStateException("Can't continue until the job is finished.");

        // There should be only the initial mappings in the category at this point.
        final var inputMappingIds = mappingRepository
            .findAllInCategory(workflow.categoryId).stream()
            .map(entity -> entity.id()).toList();

        workflow.jobId = null;
        workflow.data = new InferenceWorkflowData(
            InferenceWorkflowStep.addMappings,
            data.inputDatasourceIds(),
            data.inferenceJobId(),
            inputMappingIds
        );
    }

    private void continueAddMappings(Workflow workflow, InferenceWorkflowData data) {
        // There should be at least one mapping for the MTC job. We obviously don't count the initial mappings.
        final var mappings = mappingRepository.findAllInCategory(workflow.categoryId);
        if (mappings.size() - data.inputMappingIds().size() < 1)
            throw new IllegalArgumentException("At least one mapping is required.");

        final var jobPayloads = new ArrayList<JobPayload>();

        // First, we need to make MTC jobs for the input datasources.
        data.inputDatasourceIds().forEach(id -> {
            // TODO enable selecting only some mappings
            // jobPayloads.add(new ModelToCategoryPayload(id, null));
            jobPayloads.add(new ModelToCategoryPayload(id, List.of()));
        });

        // We need to make CTM job for each output datasource. Make sure they are unique ...
        final var outputDatasourceIds = mappings.stream()
            .filter(mapping -> !data.inputMappingIds().contains(mapping.id()))
            .map(mapping -> mapping.datasourceId).distinct().toList();

        for (final var id : outputDatasourceIds)
            // TODO enable selecting only some mappings
            // jobPayloads.add(new CategoryToModelPayload(id, null));
            jobPayloads.add(new CategoryToModelPayload(id, List.of()));

        // Only one run is created which forces all the jobs to run in the original order.
        jobService.createRun(workflow.categoryId, "Transformation", jobPayloads);

        workflow.data = data.updateStep(InferenceWorkflowStep.finish);
    }

    public InferenceWorkflowData updateWorkflowWithRestartedJob(InferenceWorkflowData data, Job newJob) {
        return new InferenceWorkflowData(
            data.step(),
            data.inputDatasourceIds(),
            newJob.id(),
            data.inputMappingIds()
        );
    }

    // #endregion

}
