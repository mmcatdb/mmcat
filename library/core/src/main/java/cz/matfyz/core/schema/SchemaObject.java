package cz.matfyz.core.schema;

import cz.matfyz.core.category.CategoricalObject;
import cz.matfyz.core.identification.Identified;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author pavel.koupil, jachymb.bartik
 */
public class SchemaObject implements CategoricalObject, Identified<Key> {
    //private static final Logger LOGGER = LoggerFactory.getLogger(SchemaObject.class);
    
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

    @Override
    public Key identifier() {
        return key;
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public SignatureId superId() {
        return superId;
    }

    /**
     * Immutable.
     */
    @Override
    public ObjectIds ids() {
        return ids;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SchemaObject schemaObject && key.equals(schemaObject.key);
    }

    /**
     * Auto-generated, constants doesn't have any special meaning.
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.key);
        return hash;
    }

    @Override
    public String toString() {
        return "SchemaObject TODO";
    }
    
}
