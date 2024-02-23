package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;

public class DeleteObject extends SchemaCategory.Editor implements SchemaModificationOperation {

    public final SchemaObject object;

    public DeleteObject(SchemaObject object) {
        this.object = object;
    }

    @Override public void up(SchemaCategory category) {
        CreateObject.assertObjectIsSingle(category, object);

        getObjectContext(category).deleteUniqueObject(object);
    }

    @Override public void down(SchemaCategory category) {
        getObjectContext(category).createUniqueObject(object);
    }

}
