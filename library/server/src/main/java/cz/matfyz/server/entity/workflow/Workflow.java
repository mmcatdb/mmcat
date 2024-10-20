package cz.matfyz.server.entity.workflow;

import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

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
    // In theory, there might be multiple jobs or inputs in a single step. It's not recommended since it might be confusing. However, the logic behind steps is entirely up the specific workflow.
    // In that case, this class holds only the current job it's waiting for.

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

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = InferenceWorkflowData.class, name = "inference"),
    })
    public interface WorkflowData {

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
