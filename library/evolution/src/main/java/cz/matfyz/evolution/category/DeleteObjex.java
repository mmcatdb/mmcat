package cz.matfyz.evolution.category;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObject;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObject;

public record DeleteObjex(
    SerializedObject schema,
    SerializedMetadataObject metadata
) implements SMO {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        CreateObjex.assertObjectIsSingle(schemaCategory, schema.deserialize());

        (new SchemaEditor(schemaCategory)).getObjects().remove(schema.key());
    }

    @Override public void down(SchemaCategory schemaCategory, MetadataCategory metadataCategory) {
        (new SchemaEditor(schemaCategory)).getObjects().put(schema.key(), schema.deserialize());
    }

}
