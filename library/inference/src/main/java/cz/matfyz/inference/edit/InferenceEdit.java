package cz.matfyz.inference.edit;

import cz.matfyz.inference.edit.algorithms.ClusterMerge;
import cz.matfyz.inference.edit.algorithms.PrimaryKeyMerge;
import cz.matfyz.inference.edit.algorithms.RecursionMerge;
import cz.matfyz.inference.edit.algorithms.ReferenceMerge;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PrimaryKeyMerge.Data.class, name = "PrimaryKey"),
    @JsonSubTypes.Type(value = ReferenceMerge.Data.class, name = "Reference"),
    @JsonSubTypes.Type(value = ClusterMerge.Data.class, name = "Cluster"),
    @JsonSubTypes.Type(value = RecursionMerge.Data.class, name = "Recursion")
})
public interface InferenceEdit extends Serializable {

    Integer getId();
    void setId(Integer id);

    boolean isActive();
    void setActive(boolean isActive);

    InferenceEditAlgorithm createAlgorithm();

}

