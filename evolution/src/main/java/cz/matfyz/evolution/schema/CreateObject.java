package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;

public class CreateObject extends SchemaCategory.Editor implements SchemaModificationOperation {

    final SchemaObject object;

    public CreateObject(SchemaObject object) {
        this.object = object;
    }

    @Override
    public void apply(SchemaCategory category) {
        getObjectContext(category).createUniqueObject(object);
    }

}
