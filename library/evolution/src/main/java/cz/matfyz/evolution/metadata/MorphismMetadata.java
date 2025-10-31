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
        if (newMorphism == null)
            metadata.setMorphism(oldMorphism.signature(), null);
        else
            metadata.setMorphism(newMorphism.signature(), newMorphism.deserialize());
    }

    @Override public void down(MetadataCategory metadata) {
        if (oldMorphism == null)
            metadata.setMorphism(newMorphism.signature(), null);
        else
            metadata.setMorphism(oldMorphism.signature(), oldMorphism.deserialize());
    }

}
