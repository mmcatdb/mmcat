package cz.matfyz.inference.edit;

import cz.matfyz.inference.edit.algorithms.ClusterMerge;
import cz.matfyz.inference.edit.algorithms.PrimaryKeyMerge;
import cz.matfyz.inference.edit.algorithms.RecursionMerge;
import cz.matfyz.inference.edit.algorithms.ReferenceMerge;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@code InferenceEdit} class is an abstract base class that represents
 * an edit operation in the inference process. Subclasses of this class
 * provide specific implementations of different types of inference edits,
 * such as merging primary keys, references, clusters, or handling recursion.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PrimaryKeyMerge.Data.class, name = "PrimaryKey"),
    @JsonSubTypes.Type(value = ReferenceMerge.Data.class, name = "Reference"),
    @JsonSubTypes.Type(value = ClusterMerge.Data.class, name = "Cluster"),
    @JsonSubTypes.Type(value = RecursionMerge.Data.class, name = "Recursion")
})
public abstract class InferenceEdit implements Serializable {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("isActive")
    private boolean isActive;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Creates an instance of the specific {@code InferenceEditAlgorithm} that
     * corresponds to the type of edit represented by this class.
     */
    public abstract InferenceEditAlgorithm createAlgorithm();

}
