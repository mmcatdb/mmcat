package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObject;

public record EditObject(
    SerializedObject newObject,
    SerializedObject oldObject
) implements SchemaModificationOperation {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schema) {
        replaceObject(schema, newObject.deserialize());
    }

    @Override public void down(SchemaCategory schema) {
        replaceObject(schema, oldObject.deserialize());
    }

    private void replaceObject(SchemaCategory schema, SchemaObject object) {
        final var objects = (new SchemaEditor(schema)).getObjects();
        // Replace the object by its newer version. The equality is determined by its key.
        objects.put(object.key(), object);

        schema.allMorphisms().forEach(morphism -> morphism.updateObject(object));
    }

}
