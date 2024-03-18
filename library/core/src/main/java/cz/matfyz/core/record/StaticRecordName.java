package cz.matfyz.core.record;

import cz.matfyz.core.mapping.StaticName.Type;

/**
 * @author jachym.bartik
 */
public class StaticRecordName extends RecordName {

    private final Type type;

    public StaticRecordName(String value, Type type) {
        super(value);
        this.type = type;
    }

    @Override public boolean equals(Object object) {
        return object instanceof StaticRecordName staticName
            && value.equals(staticName.value)
            && type.equals(staticName.type);
    }

    @Override public String toString() {
        return switch (type) {
            case STATIC -> value;
            case ANONYMOUS -> "_";
        };
    }
}
