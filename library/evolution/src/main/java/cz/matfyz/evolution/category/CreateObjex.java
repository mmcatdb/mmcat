package cz.matfyz.evolution.category;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObjex;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;
import cz.matfyz.evolution.exception.DependencyException;

import java.util.List;

public record CreateObjex(
    SerializedObjex schema,
    SerializedMetadataObjex metadata
) implements SMO {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        (new SchemaEditor(schemaCategory)).getObjexes().put(schema.key(), schema.deserialize());
    }

    @Override public void down(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        assertObjexIsSingle(schemaCategory, schema.deserialize());

        (new SchemaEditor(schemaCategory)).getObjexes().remove(schema.key());
    }

    /**
     * Check if there aren't any dependent morphisms.
     */
    static void assertObjexIsSingle(SchemaCategory schemaCategory, SchemaObjex objex) {
        final List<Signature> signaturesOfDependentMorphisms = schemaCategory.allMorphisms().stream()
            .filter(morphism -> morphism.dom().equals(objex) || morphism.cod().equals(objex))
            .map(SchemaMorphism::signature)
            .toList();

        if (!signaturesOfDependentMorphisms.isEmpty())
            throw DependencyException.objexOnMorphisms(objex.key(), signaturesOfDependentMorphisms);
    }

}
