package cz.matfyz.evolution.metadata;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.evolution.exception.EvolutionException;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ObjectMetadata.class, name = "objectMetadata"),
    @JsonSubTypes.Type(value = MorphismMetadata.class, name = "morphismMetadata"),
})
public interface MetadataModificationOperation {

    void up(MetadataCategory metadata) throws EvolutionException;

    void down(MetadataCategory metadata) throws EvolutionException;

    <T> T accept(MetadataEvolutionVisitor<T> visitor) throws EvolutionException;

}
