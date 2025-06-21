package cz.matfyz.evolution.category;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataMorphism;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;

public record DeleteMorphism(
    SerializedMorphism schema,
    SerializedMetadataMorphism metadata
) implements SMO {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        (new SchemaEditor(schemaCategory)).getMorphisms().remove(schema.signature());
    }

    @Override public void down(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        final var objexes = (new SchemaEditor(schemaCategory)).getObjexes();
        final var morphismWithObjexes = schema.deserialize(objexes::get);

        (new SchemaEditor(schemaCategory)).getMorphisms().put(morphismWithObjexes.signature(), morphismWithObjexes);
    }

}
