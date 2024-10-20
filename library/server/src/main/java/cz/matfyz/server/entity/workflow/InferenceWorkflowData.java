package cz.matfyz.server.entity.workflow;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.workflow.Workflow.WorkflowData;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public record InferenceWorkflowData(
    InferenceWorkflowStep step,
    @Nullable Id inputDatasource,
    @Nullable List<Id> allDatasources,
    @Nullable Id inferenceJob,
    @Nullable List<Id> datasourceIds,
    @Nullable List<Id> mtcJobs
) implements WorkflowData {

    public static InferenceWorkflowData createNew() {
        return new InferenceWorkflowData(
            InferenceWorkflowStep.addDatasources,
            null,
            null,
            null,
            null,
            null
        );
    }

    public enum InferenceWorkflowStep {
        addDatasources,
        editCategory,
        addMappings,
        setOutput,
        finish,
    }

}
