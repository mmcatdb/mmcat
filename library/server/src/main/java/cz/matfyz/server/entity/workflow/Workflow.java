package cz.matfyz.server.entity.workflow;

import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class Workflow extends Entity {

    public final Id categoryId;
    public final String label;
    public WorkflowState state;
    public WorkflowData data;

    private Workflow(Id id, Id categoryId, String label, WorkflowState state, WorkflowData data) {
        super(id);
        this.categoryId = categoryId;
        this.label = label;
        this.state = state;
        this.data = data;
    }

    public enum WorkflowState {
        Running,
        Finished,
        Canceled,
        Failed,
    }

    public static Workflow createNew(Id categoryId, String label, WorkflowData data) {
        return new Workflow(
            Id.createNew(),
            categoryId,
            label,
            WorkflowState.Running,
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

    public static Workflow fromJsonValue(Id id, Id categoryId, String label, WorkflowState state, String jsonValue) throws JsonProcessingException {
        final WorkflowData data = jsonValueReader.readValue(jsonValue);
        return new Workflow(
            id,
            categoryId,
            label,
            state,
            data
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(data);
    }

}
