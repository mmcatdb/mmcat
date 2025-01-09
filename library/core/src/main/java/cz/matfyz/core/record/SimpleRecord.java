package cz.matfyz.core.record;

import java.io.Serializable;

/**
 * Simple property cannot have children so it is a leaf node in the record tree.
 * However, it can have value.
 * @param <T> a type of the value of this property.
 */
public class SimpleRecord<T> implements Serializable {

    public SimpleRecord(T value) {
        this.value = value;
    }

    private final T value;

    public T getValue() {
        return value;
    }

    @Override public String toString() {
        return "\"" + value + "\"";
    }

}
