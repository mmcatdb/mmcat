package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;

public class EditObject extends SchemaCategory.Editor implements SchemaModificationOperation {

    public final SchemaObject newObject;

    public EditObject(SchemaObject newObject) {
        this.newObject = newObject;
    }

    @Override
    public void apply(SchemaCategory category) {
        final var objects = getObjectContext(category);
        // Replace the object by its newer version. The equality is determined by its key.
        objects.deleteUniqueObject(newObject);
        objects.createUniqueObject(newObject);

        getMorphismContext(category).getAllUniqueObjects().forEach(morphism -> morphism.updateObject(newObject));
    }

}
