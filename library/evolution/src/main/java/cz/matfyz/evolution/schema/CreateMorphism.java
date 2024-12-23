package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;

public record CreateMorphism(
    SerializedMorphism morphism
) implements SMO {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schema) {
        final var objects = (new SchemaEditor(schema)).getObjects();
        final var morphismWithObjects = morphism.deserialize(objects::get);

        (new SchemaEditor(schema)).getMorphisms().put(morphismWithObjects.signature(), morphismWithObjects);
    }

    @Override public void down(SchemaCategory schema) {
        (new SchemaEditor(schema)).getMorphisms().remove(morphism.signature());
    }

}
