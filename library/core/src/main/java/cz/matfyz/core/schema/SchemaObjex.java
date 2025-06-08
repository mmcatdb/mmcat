package cz.matfyz.core.schema;

import cz.matfyz.core.identifiers.Identified;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.SignatureId;

public class SchemaObjex implements Identified<SchemaObjex, Key> {

    public SchemaObjex(Key key, ObjexIds ids) {
        this.key = key;
        this.ids = ids;
        this.superId = ids.generateDefaultSuperId();
    }

    private final Key key;
    /** A unique identifier of the object (within one schema category). */
    public Key key() {
        return key;
    }

    private final ObjexIds ids;
    /** Each id is a set of signatures so that the correspondig set of attributes can unambiguosly identify this object (candidate key). */
    public ObjexIds ids() {
        return ids;
    }

    private final SignatureId superId;
    /** A union of all ids (super key). */
    public SignatureId superId() {
        return superId;
    }

    // Identification

    @Override public Key identifier() {
        return key;
    }

    @Override public boolean equals(Object other) {
        return other instanceof SchemaObjex schemaObject && key.equals(schemaObject.key);
    }

    @Override public int hashCode() {
        return key.hashCode();
    }

    // Debug

    @Override public String toString() {
        return "O: " + key;
    }

}
