package cz.matfyz.server.entity.action;

import cz.matfyz.server.entity.action.payload.CategoryToModelPayload;
import cz.matfyz.server.entity.action.payload.JsonLdToCategoryPayload;
import cz.matfyz.server.entity.action.payload.ModelToCategoryPayload;
import cz.matfyz.server.entity.action.payload.UpdateSchemaPayload;

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
    @JsonSubTypes.Type(value = UpdateSchemaPayload.class, name = "UpdateSchema"),
})
public interface ActionPayload extends Serializable {

}
