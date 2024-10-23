package cz.matfyz.server.service;

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
    private JobService jobService;

    @Autowired
    private MappingRepository mappingRepository;

    public Workflow continueWorkflow(Workflow workflow) {
        return switch (workflow.data) {
            case InferenceWorkflowData data -> continueInference(workflow, data);
            default -> throw new IllegalArgumentException("Unknown workflow type.");
        };
    }

    private Workflow continueInference(Workflow workflow, InferenceWorkflowData data) {
        switch (data.step()) {
            case selectInput -> {
                // The user has to select the input datasource. Then we can create the inference job and the user can continue.
                if (data.inputDatasourceId() == null)
                    throw new IllegalArgumentException("Input datasource is required.");

                final var payload = new RSDToCategoryPayload(data.inputDatasourceId());
                final var inferenceJob = jobService.createSystemRun(workflow.categoryId, "Schema inference", payload).job();

                final var newData = new InferenceWorkflowData(
                    InferenceWorkflowStep.editCategory,
                    data.inputDatasourceId(),
                    inferenceJob.id(),
                    data.mtcActionIds()
                );

                workflow.jobId = inferenceJob.id();
                workflow.data = newData;
                repository.save(workflow);
                return workflow;
            }
            case editCategory -> {
                // The user has to first wait for the job. Then he can check the result - it probably needs some manual adjustments. After the user marks the job as finished, he can continue.
                final Job currentJob = jobRepository.find(data.inferenceJobId()).job();
                if (currentJob.state != Job.State.Finished)
                    throw new IllegalStateException("Can't continue until the job is finished.");

                workflow.jobId = null;
                workflow.data = data.updateStep(InferenceWorkflowStep.addMappings);
                repository.save(workflow);
                return workflow;
            }
            case addMappings -> {
                // There should be at least one mapping for the MTC job. I.e., two mappings in total.
                final var mappings = mappingRepository.findAllInCategory(workflow.categoryId);
                if (mappings.size() < 2)
                    throw new IllegalArgumentException("At least two mappings are required.");

                workflow.data = data.updateStep(InferenceWorkflowStep.selectOutputs);
                repository.save(workflow);
                return workflow;
            }
            case selectOutputs -> {
                // TODO This is not clear yet. The problem is that there migh be multiple jobs, however we can only wait for one. The options are:
                //  - Allow for only one job. When it's finished, the user might go back and create another one.
                //  - Create a special type of job that contains multiple jobs and waits for all of them to finish.
                return workflow;
            }
            case finish -> {
                throw new IllegalArgumentException("The workflow is already finished.");
            }
            default -> throw new IllegalArgumentException("Unknown inference workflow step.");
        }
    }

}
