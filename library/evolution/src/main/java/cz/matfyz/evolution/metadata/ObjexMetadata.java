package cz.matfyz.evolution.metadata;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObjex;

import org.checkerframework.checker.nullness.qual.Nullable;

public record ObjexMetadata(
    @Nullable SerializedMetadataObjex newObjex,
    @Nullable SerializedMetadataObjex oldObjex
) implements MMO {

    @Override public <T> T accept(MetadataEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(MetadataCategory metadata) {
        if (newObjex == null)
            metadata.setObjex(oldObjex.key(), null);
        else
            metadata.setObjex(newObjex.key(), newObjex.deserialize());
    }

    @Override public void down(MetadataCategory metadata) {
        if (oldObjex == null)
            metadata.setObjex(newObjex.key(), null);
        else
            metadata.setObjex(oldObjex.key(), oldObjex.deserialize());
    }

}
