package cz.matfyz.evolution.schema;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObject;
import cz.matfyz.evolution.exception.DependencyException;

import java.util.List;

public record CreateObject(
    SerializedObject object
) implements SMO {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schema) {
        (new SchemaEditor(schema)).getObjects().put(object.key(), object.deserialize());
    }

    @Override public void down(SchemaCategory schema) {
        assertObjectIsSingle(schema, object.deserialize());

        (new SchemaEditor(schema)).getObjects().remove(object.key());
    }

    /**
     * Check if there aren't any dependent morphisms.
     */
    static void assertObjectIsSingle(SchemaCategory schema, SchemaObject object) {
        final List<Signature> signaturesOfDependentMorphisms = schema.allMorphisms().stream()
            .filter(morphism -> morphism.dom().equals(object) || morphism.cod().equals(object))
            .map(SchemaMorphism::signature)
            .toList();

        if (!signaturesOfDependentMorphisms.isEmpty())
            throw DependencyException.objectOnMorphisms(object.key(), signaturesOfDependentMorphisms);
    }

}
