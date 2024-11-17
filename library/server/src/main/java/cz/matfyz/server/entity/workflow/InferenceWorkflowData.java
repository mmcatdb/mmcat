package cz.matfyz.server.entity.workflow;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.workflow.Workflow.WorkflowData;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public record InferenceWorkflowData(
    InferenceWorkflowStep step,
    /** Inputs for the inference job. */
    List<Id> inputDatasourceIds,
    @Nullable Id inferenceJobId,
    /** Inputs for the transformation jobs. */
    List<Id> inputMappingIds
) implements WorkflowData {

    public static InferenceWorkflowData createNew() {
        return new InferenceWorkflowData(
            InferenceWorkflowStep.selectInputs,
            List.of(),
            null,
            new ArrayList<>()
        );
    }

    public InferenceWorkflowData updateStep(InferenceWorkflowStep step) {
        return new InferenceWorkflowData(
            step,
            inputDatasourceIds,
            inferenceJobId,
            inputMappingIds
        );
    }

    public enum InferenceWorkflowStep {
        selectInputs,
        editCategory,
        addMappings,
        finish,
    }

}
