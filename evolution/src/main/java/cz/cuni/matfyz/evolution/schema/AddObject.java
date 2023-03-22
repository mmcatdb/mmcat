package cz.cuni.matfyz.evolution.schema;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;

public class AddObject implements SchemaModificationOperation {

    final SchemaObject object;

    public AddObject(SchemaObject object) {
        this.object = object;
    }

    @Override
    public void apply(SchemaCategory category) {
        category.addObject(object);
    }

}
