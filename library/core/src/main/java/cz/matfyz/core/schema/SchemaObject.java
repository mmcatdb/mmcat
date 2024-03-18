package cz.matfyz.core.schema;

import cz.matfyz.core.identifiers.Identified;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.SignatureId;

/**
 * @author pavel.koupil, jachymb.bartik
 */
public class SchemaObject implements Identified<SchemaObject, Key> {

    /** A unique identifier of the object (within one schema category). */
    private final Key key;
    /** A user-readable label. */
    private final String label;
    /** Each id is a set of signatures so that the correspondig set of attributes can unambiguosly identify this object (candidate key). */
    private final ObjectIds ids;
    /** A union of all ids (super key). */
    private final SignatureId superId;

    public SchemaObject(Key key, String label, ObjectIds ids, SignatureId superId) {
        this.key = key;
        this.label = label;
        this.ids = ids;
        this.superId = superId;
    }

    public Key key() {
        return key;
    }

    public String label() {
        return label;
    }

    public SignatureId superId() {
        return superId;
    }

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

    // Identification

    @Override public String toString() {
        return "SO: " + key;
    }

}
