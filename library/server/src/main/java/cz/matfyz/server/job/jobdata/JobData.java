package cz.matfyz.server.job.jobdata;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ModelJobData.class, name = "Model"),
    @JsonSubTypes.Type(value = InferenceJobData.class, name = "Inference"),
})
public interface JobData extends Serializable {

}
