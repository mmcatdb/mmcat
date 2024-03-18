package cz.matfyz.core.record;

import cz.matfyz.core.identifiers.Signature;

/**
 * Simple property cannot have children so it is a leaf node in the record tree.
 * However, it can have value.
 * @author jachymb.bartik
 * @param <T> a type of the value of this property.
 */
public abstract class SimpleRecord<T> extends DataRecord {

    protected final Signature signature;

    SimpleRecord(RecordName name, Signature signature) {
        super(name);
        this.signature = signature;
    }

    public Signature signature() {
        return signature;
    }

}
