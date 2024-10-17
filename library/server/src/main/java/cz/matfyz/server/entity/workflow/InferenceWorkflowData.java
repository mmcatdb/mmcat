package cz.matfyz.server.entity.workflow;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.workflow.Workflow.WorkflowData;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public record InferenceWorkflowData(
    @Nullable List<Id> datasourceIds,
    @Nullable Id jobId
) implements WorkflowData {

    public static InferenceWorkflowData createNew() {
        return new InferenceWorkflowData(null, null);
    }
}
