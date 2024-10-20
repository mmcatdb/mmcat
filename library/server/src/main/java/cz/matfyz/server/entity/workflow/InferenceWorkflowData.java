package cz.matfyz.server.entity.workflow;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.workflow.Workflow.WorkflowData;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public record InferenceWorkflowData(
    InferenceWorkflowStep step,
    @Nullable Id inputDatasourceId,
    List<Id> allDatasourceIds,
    @Nullable Id inferenceJobId,
    List<Id> mtcActionIds
) implements WorkflowData {

    public static InferenceWorkflowData createNew() {
        return new InferenceWorkflowData(
            InferenceWorkflowStep.addDatasources,
            null,
            new ArrayList<>(),
            null,
            new ArrayList<>()
        );
    }

    public InferenceWorkflowData updateStep(InferenceWorkflowStep step) {
        return new InferenceWorkflowData(
            step,
            inputDatasourceId,
            allDatasourceIds,
            inferenceJobId,
            mtcActionIds
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
