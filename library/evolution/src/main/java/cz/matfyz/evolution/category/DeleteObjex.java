package cz.matfyz.evolution.category;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObjex;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;

public record DeleteObjex(
    SerializedObjex schema,
    SerializedMetadataObjex metadata
) implements SMO {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        CreateObjex.assertObjexIsSingle(schemaCategory, schema.deserialize());

        (new SchemaEditor(schemaCategory)).getObjexes().remove(schema.key());
    }

    @Override public void down(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        (new SchemaEditor(schemaCategory)).getObjexes().put(schema.key(), schema.deserialize());
    }

}
