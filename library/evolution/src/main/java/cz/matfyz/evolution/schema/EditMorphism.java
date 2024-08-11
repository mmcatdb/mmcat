package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;

public record EditMorphism(
    SerializedMorphism newMorphism,
    SerializedMorphism oldMorphism
) implements SchemaModificationOperation {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schema) {
        replaceMorphism(schema, newMorphism);
    }

    @Override public void down(SchemaCategory schema) {
        replaceMorphism(schema, oldMorphism);
    }

    private void replaceMorphism(SchemaCategory schema, SerializedMorphism morphism) {
        final var objects = (new SchemaEditor(schema)).getObjects();
        final var morphismWithObjects = morphism.deserialize(objects::get);

        final var morphisms = (new SchemaEditor(schema)).getMorphisms();
        // Replace the morphism by its newer version. The equality is determined by its signature.
        morphisms.put(morphismWithObjects.signature(), morphismWithObjects);
    }

}
