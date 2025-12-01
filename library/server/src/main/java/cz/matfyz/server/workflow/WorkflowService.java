package cz.matfyz.server.workflow;

import cz.matfyz.server.inference.InferenceService;
import cz.matfyz.server.inference.InferenceWorkflowData;
import cz.matfyz.server.job.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkflowService {

    @Autowired
    private WorkflowRepository repository;

    @Autowired
    private InferenceService inferenceService;

    public Workflow continueWorkflow(Workflow workflow) {
        workflow = switch (workflow.data) {
            case InferenceWorkflowData data -> inferenceService.continueWorkflow(workflow, data);
            default -> throw new IllegalArgumentException("Unknown workflow type.");
        };

        repository.save(workflow);

        return workflow;
    }

    public void updateWorkflowsWithRestartedJob(Job oldJob, Job newJob) {
        final var dependentWorkflows = repository.findAllByJob(oldJob.id());
        for (final var workflow : dependentWorkflows) {
            workflow.jobId = newJob.id();

            workflow.data = switch (workflow.data) {
                case InferenceWorkflowData data -> inferenceService.updateWorkflowWithRestartedJob(data, newJob);
                default -> throw new IllegalArgumentException("Unknown workflow type.");
            };

            repository.save(workflow);
        }
    }

}
