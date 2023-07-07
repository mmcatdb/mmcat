package cz.cuni.matfyz.server.entity.job;

import cz.cuni.matfyz.server.entity.job.payload.CategoryToModelPayload;
import cz.cuni.matfyz.server.entity.job.payload.JsonLdToCategoryPayload;
import cz.cuni.matfyz.server.entity.job.payload.ModelToCategoryPayload;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author jachym.bartik
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CategoryToModelPayload.class, name = "CategoryToModel"),
    @JsonSubTypes.Type(value = ModelToCategoryPayload.class, name = "ModelToCategory"),
    @JsonSubTypes.Type(value = JsonLdToCategoryPayload.class, name = "JsonLdToCategory"),
})
public interface JobPayload extends Serializable {
    
    public interface Detail extends Serializable {

    }

}
