package cz.matfyz.evolution.category;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.exception.EvolutionException;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CreateObjex.class, name = "createObjex"),
    @JsonSubTypes.Type(value = DeleteObjex.class, name = "deleteObjex"),
    @JsonSubTypes.Type(value = UpdateObjex.class, name = "updateObjex"),
    @JsonSubTypes.Type(value = CreateMorphism.class, name = "createMorphism"),
    @JsonSubTypes.Type(value = DeleteMorphism.class, name = "deleteMorphism"),
    @JsonSubTypes.Type(value = UpdateMorphism.class, name = "updateMorphism"),
    @JsonSubTypes.Type(value = Composite.class, name = "composite"),
})
public interface SMO {

    <T> T accept(SchemaEvolutionVisitor<T> visitor) throws EvolutionException;

    void up(SchemaCategory schema, MetadataCategory metadata) throws EvolutionException;

    void down(SchemaCategory schema, MetadataCategory metadata) throws EvolutionException;

}
