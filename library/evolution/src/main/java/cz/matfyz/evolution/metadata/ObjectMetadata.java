package cz.matfyz.evolution.metadata;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObject;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ObjectMetadata extends MetadataCategory.Editor implements MetadataModificationOperation {

    @Override public <T> T accept(MetadataEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public final @Nullable SerializedMetadataObject newObject;
    public final @Nullable SerializedMetadataObject oldObject;

    // TODO maybe split into multiple operations instead of the nulls?

    public ObjectMetadata(@Nullable SerializedMetadataObject newObject, @Nullable SerializedMetadataObject oldObject) {
        this.newObject = newObject;
        this.oldObject = oldObject;
    }

    @Override public void up(MetadataCategory schema) {
        final var objects = getObjects(schema);

        if (newObject == null)
            objects.remove(oldObject.key());
        else
            objects.put(newObject.key(), newObject.deserialize());
    }

    @Override public void down(MetadataCategory schema) {
        final var objects = getObjects(schema);

        if (oldObject == null)
            objects.remove(newObject.key());
        else
            objects.put(oldObject.key(), oldObject.deserialize());
    }

}
