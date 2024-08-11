package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObject;

public record DeleteObject(
    SerializedObject object
) implements SchemaModificationOperation {

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public void up(SchemaCategory schema) {
        CreateObject.assertObjectIsSingle(schema, object.deserialize());

        (new SchemaEditor(schema)).getObjects().remove(object.key());
    }

    @Override public void down(SchemaCategory schema) {
        (new SchemaEditor(schema)).getObjects().put(object.key(), object.deserialize());
    }

}
