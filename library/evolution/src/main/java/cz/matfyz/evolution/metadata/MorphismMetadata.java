package cz.matfyz.evolution.metadata;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataMorphism;

import org.checkerframework.checker.nullness.qual.Nullable;

public record MorphismMetadata(
    @Nullable SerializedMetadataMorphism newMorphism,
    @Nullable SerializedMetadataMorphism oldMorphism
) implements MMO {

    @Override public <T> T accept(MetadataEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(MetadataCategory metadata) {
        final var morphisms = (new MetadataEditor(metadata)).getMorphisms();

        if (newMorphism == null)
            morphisms.remove(oldMorphism.signature());
        else
            morphisms.put(newMorphism.signature(), newMorphism.deserialize());
    }

    @Override public void down(MetadataCategory metadata) {
        final var morphisms = (new MetadataEditor(metadata)).getMorphisms();

        if (oldMorphism == null)
            morphisms.remove(newMorphism.signature());
        else
            morphisms.put(oldMorphism.signature(), oldMorphism.deserialize());
    }

}
