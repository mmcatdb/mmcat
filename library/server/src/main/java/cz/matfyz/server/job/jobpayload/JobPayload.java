package cz.matfyz.server.job.jobpayload;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CategoryToModelPayload.class, name = "CategoryToModel"),
    @JsonSubTypes.Type(value = ModelToCategoryPayload.class, name = "ModelToCategory"),
    @JsonSubTypes.Type(value = UpdateSchemaPayload.class, name = "UpdateSchema"),
    @JsonSubTypes.Type(value = RSDToCategoryPayload.class, name = "RSDToCategory"),
})
public interface JobPayload extends Serializable {

    static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JobPayload.class);

    public static JobPayload fromJsonValue(String jsonValue) throws JsonProcessingException {
        return jsonValueReader.readValue(jsonValue);
    }

}
