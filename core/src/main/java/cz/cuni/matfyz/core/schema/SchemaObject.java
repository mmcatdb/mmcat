package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.CategoricalObject;
import cz.cuni.matfyz.core.identification.Identified;

import java.util.Objects;

/**
 * @author pavel.koupil, jachymb.bartik
 */
public class SchemaObject implements CategoricalObject, Identified<Key> {
    //private static final Logger LOGGER = LoggerFactory.getLogger(SchemaObject.class);
    
    private final Key key; // Identifies the object, in the paper it's a number >= 100
    private final String label;
    private final SignatureId superId; // Should be a union of all ids (super key).
    private final ObjectIds ids; // Each id is a set of signatures so that the correspondig set of attributes can unambiguosly identify this object (candidate key).

    public SchemaObject(Key key, String label, SignatureId superId, ObjectIds ids) {
        this(key, label, superId, ids, "", "");
    }

    public final String iri;
    public final String pimIri;

    public SchemaObject(Key key, String label, SignatureId superId, ObjectIds ids, String iri, String pimIri) {
        this.key = key;
        this.label = label;
        this.superId = superId;
        this.ids = ids;
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
    public int compareTo(CategoricalObject categoricalObject) {
        return key.compareTo(categoricalObject.key());
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
