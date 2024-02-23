package cz.matfyz.core.record;

import cz.matfyz.core.category.Signature;

import java.util.List;

/**
 * @author jachymb.bartik
 */
public class SimpleArrayRecord<T> extends SimpleRecord<T> {

    private final List<T> values;

    SimpleArrayRecord(RecordName name, Signature signature, List<T> values) {
        super(name, signature);
        this.values = values;
    }

    public List<T> getValues() {
        return values;
    }

    @Override public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(": [");
        if (!values.isEmpty())
            builder.append("\"").append(values.get(0)).append("\"");
        for (int i = 1; i < values.size(); i++)
            builder.append(", \"").append(values.get(i)).append("\"");
        builder.append("]");

        return builder.toString();
    }
}
