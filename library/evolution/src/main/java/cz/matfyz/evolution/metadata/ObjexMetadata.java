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
        final var objexes = (new MetadataEditor(metadata)).getObjexes();

        if (newObjex == null)
            objexes.remove(oldObjex.key());
        else
            objexes.put(newObjex.key(), newObjex.deserialize());
    }

    @Override public void down(MetadataCategory metadata) {
        final var objexes = (new MetadataEditor(metadata)).getObjexes();

        if (oldObjex == null)
            objexes.remove(newObjex.key());
        else
            objexes.put(oldObjex.key(), oldObjex.deserialize());
    }

}
