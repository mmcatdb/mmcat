package cz.matfyz.evolution.metadata;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObject;

import org.checkerframework.checker.nullness.qual.Nullable;

public record ObjectMetadata(
    @Nullable SerializedMetadataObject newObject,
    @Nullable SerializedMetadataObject oldObject
) implements MMO {

    @Override public <T> T accept(MetadataEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(MetadataCategory metadata) {
        final var objects = (new MetadataEditor(metadata)).getObjects();

        if (newObject == null)
            objects.remove(oldObject.key());
        else
            objects.put(newObject.key(), newObject.deserialize());
    }

    @Override public void down(MetadataCategory metadata) {
        final var objects = (new MetadataEditor(metadata)).getObjects();

        if (oldObject == null)
            objects.remove(newObject.key());
        else
            objects.put(oldObject.key(), oldObject.deserialize());
    }

}
