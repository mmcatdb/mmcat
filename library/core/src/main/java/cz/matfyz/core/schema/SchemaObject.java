package cz.matfyz.core.schema;

import cz.matfyz.core.identifiers.Identified;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.SignatureId;

public class SchemaObject implements Identified<SchemaObject, Key> {


    public SchemaObject(Key key, ObjectIds ids, SignatureId superId) {
        this.key = key;
        this.ids = ids;
        this.superId = superId;
    }

    private final Key key;
    /** A unique identifier of the object (within one schema category). */
    public Key key() {
        return key;
    }

    private final ObjectIds ids;
    /** Each id is a set of signatures so that the correspondig set of attributes can unambiguosly identify this object (candidate key). */
    public SignatureId superId() {
        return superId;
    }

    private final SignatureId superId;
    /** A union of all ids (super key). */
    public ObjectIds ids() {
        return ids;
    }

    // Identification

    @Override public Key identifier() {
        return key;
    }

    @Override public boolean equals(Object other) {
        return other instanceof SchemaObject schemaObject && key.equals(schemaObject.key);
    }

    @Override public int hashCode() {
        return key.hashCode();
    }

    // Debug

    @Override public String toString() {
        return "O: " + key;
    }

}
