package cz.matfyz.server.service;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.payload.CategoryToModelPayload;
import cz.matfyz.server.entity.action.payload.ModelToCategoryPayload;
import cz.matfyz.server.entity.action.payload.RSDToCategoryPayload;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.workflow.InferenceWorkflowData;
import cz.matfyz.server.entity.workflow.Workflow;
import cz.matfyz.server.entity.workflow.InferenceWorkflowData.InferenceWorkflowStep;
import cz.matfyz.server.repository.JobRepository;
import cz.matfyz.server.repository.MappingRepository;
import cz.matfyz.server.repository.WorkflowRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkflowService {

    @Autowired
    private WorkflowRepository repository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ActionService actionService;

    @Autowired
    private JobService jobService;

    @Autowired
    private MappingRepository mappingRepository;

    public Workflow continueWorkflow(Workflow workflow, Id sessionId) {
        return switch (workflow.data) {
            case InferenceWorkflowData data -> continueInference(workflow, data, sessionId);
            default -> throw new IllegalArgumentException("Unknown workflow type.");
        };
    }

    private Workflow continueInference(Workflow workflow, InferenceWorkflowData data, Id sessionId) {
        switch (data.step()) {
            case selectInputs -> {
                // The user has to select the input datasource. Then we can create the inference job and the user can continue.
                if (data.inputDatasourceIds().isEmpty())
                    throw new IllegalArgumentException("Input datasource is required.");

                final var action = actionService.create(workflow.categoryId, "Schema inference", new RSDToCategoryPayload(data.inputDatasourceIds()));
                final var inferenceJob = jobService.createUserRun(action, sessionId).job();

                final var newData = new InferenceWorkflowData(
                    InferenceWorkflowStep.editCategory,
                    data.inputDatasourceIds(),
                    inferenceJob.id(),
                    data.inputMappingIds()
                );

                workflow.jobId = inferenceJob.id();
                workflow.data = newData;
                repository.save(workflow);
                return workflow;
            }
            case editCategory -> {
                // The user has to wait for the job first. Then he can check the result - it probably needs some manual adjustments. After the user marks the job as finished, he can continue.
                final Job currentJob = jobRepository.find(data.inferenceJobId()).job();
                if (currentJob.state != Job.State.Finished)
                    throw new IllegalStateException("Can't continue until the job is finished.");

                // There should be only the initial mappings in the category at this point.
                final var inputMappingIds = mappingRepository
                    .findAllInCategory(workflow.categoryId).stream()
                    .map(wrapper -> wrapper.id()).toList();

                workflow.jobId = null;
                workflow.data = new InferenceWorkflowData(
                    InferenceWorkflowStep.addMappings,
                    data.inputDatasourceIds(),
                    data.inferenceJobId(),
                    inputMappingIds
                );

                repository.save(workflow);
                return workflow;
            }
            case addMappings -> {
                // There should be at least one mapping for the MTC job. We obviously don't count the initial mappings.
                final var mappings = mappingRepository.findAllInCategory(workflow.categoryId);
                if (mappings.size() - data.inputMappingIds().size() < 1)
                    throw new IllegalArgumentException("At least one mapping is required.");

                // First, we need to make MTC jobs for the input datasource.
                data.inputDatasourceIds().forEach(id -> {
                    final var mtcAction = actionService.create(workflow.categoryId, "Input", new ModelToCategoryPayload(id));
                    jobService.createUserRun(mtcAction, sessionId);
                });

                // We need to make CTM job for each output datasource. Make sure they are unique ...
                final var outputDatasourceIds = mappings.stream()
                    .filter(mapping -> !data.inputMappingIds().contains(mapping.id()))
                    .map(mapping -> mapping.datasourceId).distinct().toList();

                for (final var id : outputDatasourceIds) {
                    final var ctmAction = actionService.create(workflow.categoryId, "Output", new CategoryToModelPayload(id));
                    jobService.createUserRun(ctmAction, sessionId);
                }

                workflow.data = data.updateStep(InferenceWorkflowStep.finish);
                repository.save(workflow);
                return workflow;
            }
            case finish -> {
                throw new IllegalArgumentException("The workflow is already finished.");
            }
            default -> throw new IllegalArgumentException("Unknown inference workflow step.");
        }
    }

}
