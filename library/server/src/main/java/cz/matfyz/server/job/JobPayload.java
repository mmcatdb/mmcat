package cz.matfyz.server.job;

import cz.matfyz.server.evolution.SchemaEvolutionPayload;
import cz.matfyz.server.inference.InferencePayload;
import cz.matfyz.server.instance.CategoryToModelPayload;
import cz.matfyz.server.instance.ModelToCategoryPayload;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CategoryToModelPayload.class, name = "CategoryToModel"),
    @JsonSubTypes.Type(value = ModelToCategoryPayload.class, name = "ModelToCategory"),
    @JsonSubTypes.Type(value = SchemaEvolutionPayload.class, name = "SchemaEvolution"),
    @JsonSubTypes.Type(value = InferencePayload.class, name = "Inference"),
})
public interface JobPayload extends Serializable {

    static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JobPayload.class);

    static JobPayload fromJsonValue(String jsonValue) throws JsonProcessingException {
        return jsonValueReader.readValue(jsonValue);
    }

    /** The job doesn't have to wait for a user's confirmation to start. */
    @JsonIgnore
    default boolean isStartedAutomatically() {
        return true;
    }

    /** The job is finished as soon as its first execution ends. */
    @JsonIgnore
    default boolean isFinishedAutomatically() {
        return true;
    }

}
