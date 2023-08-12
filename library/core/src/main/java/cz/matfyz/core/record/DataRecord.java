package cz.matfyz.core.record;

import java.io.Serializable;

/**
 * This class represents a general node of the record tree. Record was already taken by java ...
 * @author jachymb.bartik
 */
public abstract class DataRecord implements Serializable {
    
    protected final RecordName name;
    
    protected DataRecord(RecordName name) {
        this.name = name;
    }
    
    public RecordName name() {
        return this.name;
    }
    
    // Iterate through all simple properties of this tree
    // public abstract Set<DataRecord> records();
}
