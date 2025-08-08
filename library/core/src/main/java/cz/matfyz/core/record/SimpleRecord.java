package cz.matfyz.core.record;

import java.io.Serializable;

/**
 * Simple property cannot have children so it is a leaf node in the record tree.
 * However, it can have value.
 */
class SimpleRecord<TDataType> implements Serializable {

    SimpleRecord(TDataType value) {
        this.value = value;
    }

    private final TDataType value;

    TDataType getValue() {
        return value;
    }

    @Override public String toString() {
        return "\"" + value + "\"";
    }

}
