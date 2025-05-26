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
        replaceMorphism(schema, newMorphism);
    }

    @Override public void down(SchemaCategory schema, MetadataCategory metadataCategory) {
        replaceMorphism(schema, oldMorphism);
    }

    private void replaceMorphism(SchemaCategory schema, SerializedMorphism morphism) {
        final var objexes = (new SchemaEditor(schema)).getObjexes();
        final var morphismWithObjexes = morphism.deserialize(objexes::get);

        final var morphisms = (new SchemaEditor(schema)).getMorphisms();
        // Replace the morphism by its newer version. The equality is determined by its signature.
        morphisms.put(morphismWithObjexes.signature(), morphismWithObjexes);
    }

}
