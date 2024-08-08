package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;

public class EditObject extends SchemaCategory.Editor implements SchemaModificationOperation {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public final SchemaObject newObject;
    public final SchemaObject oldObject;

    public EditObject(SchemaObject newObject, SchemaObject oldObject) {
        this.newObject = newObject;
        this.oldObject = oldObject;
    }

    @Override public void up(SchemaCategory category) {
        replaceObject(category, newObject);
    }

    @Override public void down(SchemaCategory category) {
        replaceObject(category, oldObject);
    }

    private void replaceObject(SchemaCategory category, SchemaObject object) {
        final var objects = getObjects(category);
        // Replace the object by its newer version. The equality is determined by its key.
        objects.remove(object.key());
        objects.put(object.key(), object);

        category.allMorphisms().forEach(morphism -> morphism.updateObject(object));
    }

}
