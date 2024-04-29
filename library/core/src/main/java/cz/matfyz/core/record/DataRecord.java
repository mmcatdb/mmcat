package cz.matfyz.core.record;

import java.io.Serializable;

/**
 * This class represents a general node of the record tree. Record was already taken by java ...
 */
public abstract class DataRecord implements Serializable {

    protected final RecordName name;

    protected DataRecord(RecordName name) {
        this.name = name;
    }

    public RecordName name() {
        return this.name;
    }

}
