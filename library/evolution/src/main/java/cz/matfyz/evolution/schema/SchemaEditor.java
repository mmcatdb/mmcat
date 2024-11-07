package cz.matfyz.evolution.schema;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.util.Map;

// TODO unify all schema editors in some way ...
public class SchemaEditor extends SchemaCategory.Editor {

    private final SchemaCategory schema;

    public SchemaEditor(SchemaCategory schema) {
        this.schema = schema;
    }

    public Map<Key, SchemaObject> getObjects() {
        return getObjects(schema);
    }

    public Map<Signature, SchemaMorphism> getMorphisms() {
        return getMorphisms(schema);
    }

}
