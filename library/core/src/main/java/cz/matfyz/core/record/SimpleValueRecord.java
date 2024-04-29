package cz.matfyz.core.record;

import cz.matfyz.core.identifiers.Signature;

public class SimpleValueRecord<T> extends SimpleRecord<T> {

    private final T value;

    SimpleValueRecord(RecordName name, Signature signature, T value) {
        super(name, signature);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(": \"").append(value).append("\"");

        return builder.toString();
    }
}
