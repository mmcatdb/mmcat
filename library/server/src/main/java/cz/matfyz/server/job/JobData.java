package cz.matfyz.server.job;

import cz.matfyz.server.inference.InferenceJobData;
import cz.matfyz.server.instance.TransformationJobData;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TransformationJobData.class, name = "Transformation"),
    @JsonSubTypes.Type(value = InferenceJobData.class, name = "Inference"),
})
public interface JobData extends Serializable {

}
