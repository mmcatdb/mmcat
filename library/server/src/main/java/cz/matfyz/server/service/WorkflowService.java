package cz.matfyz.server.service;

import cz.matfyz.server.entity.action.payload.RSDToCategoryPayload;
import cz.matfyz.server.entity.job.Job;
import cz.matfyz.server.entity.workflow.InferenceWorkflowData;
import cz.matfyz.server.entity.workflow.Workflow;
import cz.matfyz.server.entity.workflow.InferenceWorkflowData.InferenceWorkflowStep;
import cz.matfyz.server.repository.JobRepository;
import cz.matfyz.server.repository.WorkflowRepository;

import org.checkerframework.checker.nullness.qual.Nullable;
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

    public Workflow continueWorkflow(Workflow workflow) {
        // If the workflow is waiting for a job, it has to be finished before we can continue.
        final @Nullable Job currentJob = workflow.jobId == null ? null : jobRepository.find(workflow.jobId).job();
        if (currentJob != null) {
            if (currentJob.state != Job.State.Finished)
                throw new IllegalStateException("Can't continue until the job is finished.");

            // The job is finished, we remove it from the workflow.
            workflow.jobId = null;
        }

        // The job is either finished or it doesn't exist. Now we have to decide based on the workflow data.
        return switch (workflow.data) {
            case InferenceWorkflowData data -> continueInference(workflow, data, currentJob);
            default -> throw new IllegalArgumentException("Unknown workflow type.");
        };
    }

    private Workflow continueInference(Workflow workflow, InferenceWorkflowData data, @Nullable Job currentJob) {
        switch (data.step()) {
            case selectInput -> {
                if (currentJob != null) {
                    // The inference job is finished - we can continue.
                    workflow.data = data.updateStep(InferenceWorkflowStep.editCategory);
                    repository.save(workflow);
                    return workflow;
                }

                // The inference job doesn't exist yet. We have to create it.

                if (data.inputDatasourceId() == null)
                    throw new IllegalArgumentException("Input datasource is required.");

                final var payload = new RSDToCategoryPayload(data.inputDatasourceId());
                final var inferenceJob = jobService.createSystemRun(workflow.categoryId, "Schema inference", payload).job();

                final var newData = new InferenceWorkflowData(
                    InferenceWorkflowStep.selectInput,
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
                // There are no check here - if the user wants to continue, he can. There's no way back tho.
                workflow.data = data.updateStep(InferenceWorkflowStep.addMappings);
                repository.save(workflow);
                return workflow;
            }
            case addMappings -> {
                // There should be at least one mapping for the MTC job ... but it's not checked here.
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
