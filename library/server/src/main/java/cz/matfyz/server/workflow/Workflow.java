package cz.matfyz.server.workflow;

import cz.matfyz.server.utils.entity.Entity;
import cz.matfyz.server.utils.entity.Id;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Workflow extends Entity {

    // The general idea is that a workflow is a sequence of steps. Each step might require user input, job, or both.
    // If a step requires a job, it waits for the job to finish. Only after that can the user continue.
    // There might be only multiple waiting jobs for a step (it's up to the workflow to decide when to continue). However, it isn't recommended since it might be confusing for the users.
    // This class holds only the current job the workflow is waiting for, not the previous ones.

    public final Id categoryId;
    public final String label;
    /** The workflow is waiting for a job. If not provided, the workflow is waiting for the user rather for the job. */
    public @Nullable Id jobId;
    public WorkflowData data;

    private Workflow(Id id, Id categoryId, String label, @Nullable Id jobId, WorkflowData data) {
        super(id);
        this.categoryId = categoryId;
        this.label = label;
        this.jobId = jobId;
        this.data = data;
    }

    public static Workflow createNew(Id categoryId, String label, WorkflowData data) {
        return new Workflow(
            Id.createNew(),
            categoryId,
            label,
            null,
            data
        );
    }

    public enum WorkflowType {
        inference,
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = InferenceWorkflowData.class, name = "inference"),
    })
    public interface WorkflowData {

        public static WorkflowData createNew(WorkflowType type) {
            return switch (type) {
                case inference -> InferenceWorkflowData.createNew();
            };
        }

    }

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(WorkflowData.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(WorkflowData.class);

    public static Workflow fromJsonValue(Id id, Id categoryId, String label, Id jobId, String jsonValue) throws JsonProcessingException {
        final WorkflowData data = jsonValueReader.readValue(jsonValue);
        return new Workflow(
            id,
            categoryId,
            label,
            jobId,
            data
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(data);
    }

}
