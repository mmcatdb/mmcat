package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;

public class DeleteObject extends SchemaCategory.Editor implements SchemaModificationOperation {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public final SchemaObject object;

    public DeleteObject(SchemaObject object) {
        this.object = object;
    }

    @Override public void up(SchemaCategory category) {
        CreateObject.assertObjectIsSingle(category, object);

        getObjects(category).remove(object.key());
    }

    @Override public void down(SchemaCategory category) {
        getObjects(category).put(object.key(), object);
    }

}
