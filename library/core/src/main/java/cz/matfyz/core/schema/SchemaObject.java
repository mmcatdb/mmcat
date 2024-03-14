package cz.matfyz.core.schema;

import cz.matfyz.core.identifiers.Identified;
import cz.matfyz.core.identifiers.Key;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author pavel.koupil, jachymb.bartik
 */
public class SchemaObject implements Identified<SchemaObject, Key> {

    private final Key key; // Identifies the object, in the paper it's a number >= 100
    private final String label;
    private final ObjectIds ids; // Each id is a set of signatures so that the correspondig set of attributes can unambiguosly identify this object (candidate key).
    private final SignatureId superId; // Should be a union of all ids (super key).
    @Nullable
    public final String iri;
    @Nullable
    public final String pimIri;

    public SchemaObject(Key key, String label, ObjectIds ids, SignatureId superId, String iri, String pimIri) {
        this.key = key;
        this.label = label;
        this.ids = ids;
        this.superId = superId;
        this.iri = iri;
        this.pimIri = pimIri;
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

    /**
     * Immutable.
     */
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
