package cz.matfyz.evolution.category;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;

public record UpdateMorphism(
    SerializedMorphism newMorphism,
    SerializedMorphism oldMorphism
) implements SMO {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schema, MetadataCategory metadataCategory) {
        schema.replaceMorphism(newMorphism);
    }

    @Override public void down(SchemaCategory schema, MetadataCategory metadataCategory) {
        schema.replaceMorphism(oldMorphism);
    }

}
