package cz.cuni.matfyz.evolution.schema;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;

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
