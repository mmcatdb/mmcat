package cz.matfyz.evolution.metadata;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataMorphism;

import org.checkerframework.checker.nullness.qual.Nullable;

public class MorphismMetadata extends MetadataCategory.Editor implements MetadataModificationOperation {

    @Override public <T> T accept(MetadataEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public final @Nullable SerializedMetadataMorphism newMorphism;
    public final @Nullable SerializedMetadataMorphism oldMorphism;

    public MorphismMetadata(@Nullable SerializedMetadataMorphism newMorphism, @Nullable SerializedMetadataMorphism oldMorphism) {
        this.newMorphism = newMorphism;
        this.oldMorphism = oldMorphism;
    }

    @Override public void up(MetadataCategory metadata) {
        final var morphisms = getMorphisms(metadata);

        if (newMorphism == null)
            morphisms.remove(oldMorphism.signature());
        else
            morphisms.put(newMorphism.signature(), newMorphism.deserialize());
    }

    @Override public void down(MetadataCategory metadata) {
        final var morphisms = getMorphisms(metadata);

        if (oldMorphism == null)
            morphisms.remove(newMorphism.signature());
        else
            morphisms.put(oldMorphism.signature(), oldMorphism.deserialize());
    }

}
