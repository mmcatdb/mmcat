package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.exception.EvolutionException;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CreateObject.class, name = "createObject"),
    @JsonSubTypes.Type(value = DeleteObject.class, name = "deleteObject"),
    @JsonSubTypes.Type(value = UpdateObject.class, name = "updateObject"),
    @JsonSubTypes.Type(value = CreateMorphism.class, name = "createMorphism"),
    @JsonSubTypes.Type(value = DeleteMorphism.class, name = "deleteMorphism"),
    @JsonSubTypes.Type(value = UpdateMorphism.class, name = "updateMorphism"),
    @JsonSubTypes.Type(value = Composite.class, name = "composite"),
})
public interface SMO {

    void up(SchemaCategory schema) throws EvolutionException;

    void down(SchemaCategory schema) throws EvolutionException;

    <T> T accept(SchemaEvolutionVisitor<T> visitor) throws EvolutionException;

}
