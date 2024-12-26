package cz.matfyz.evolution.category;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObject;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObject;
import cz.matfyz.evolution.exception.DependencyException;

import java.util.List;

public record CreateObject(
    SerializedObject schema,
    SerializedMetadataObject metadata
) implements SMO {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        (new SchemaEditor(schemaCategory)).getObjects().put(schema.key(), schema.deserialize());
    }

    @Override public void down(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        assertObjectIsSingle(schemaCategory, schema.deserialize());

        (new SchemaEditor(schemaCategory)).getObjects().remove(schema.key());
    }

    /**
     * Check if there aren't any dependent morphisms.
     */
    static void assertObjectIsSingle(SchemaCategory schemaCategory, SchemaObject object) {
        final List<Signature> signaturesOfDependentMorphisms = schemaCategory.allMorphisms().stream()
            .filter(morphism -> morphism.dom().equals(object) || morphism.cod().equals(object))
            .map(SchemaMorphism::signature)
            .toList();

        if (!signaturesOfDependentMorphisms.isEmpty())
            throw DependencyException.objectOnMorphisms(object.key(), signaturesOfDependentMorphisms);
    }

}
