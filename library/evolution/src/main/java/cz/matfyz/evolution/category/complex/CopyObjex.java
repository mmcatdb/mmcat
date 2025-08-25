package cz.matfyz.evolution.category.complex;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObjex;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;
import cz.matfyz.evolution.category.SchemaEvolutionVisitor;
import cz.matfyz.evolution.category.SMO;

public record CopyObjex(
    SerializedObjex source,
    SerializedObjex target,
    SerializedMetadataObjex sourceMetadata,
    SerializedMetadataObjex targetMetadata
) implements SMO {

    @Override
    public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void up(SchemaCategory schema, MetadataCategory metadata) {
        //
    }

    @Override
    public void down(SchemaCategory schema, MetadataCategory metadata) {
        // No need to do anything here since we didn't modify the schema
        // The target objex will be deleted by a separate DeleteObjex operation if needed
    }
}
