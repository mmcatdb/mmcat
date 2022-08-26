package cz.cuni.matfyz.core.record;

import cz.cuni.matfyz.core.category.Signature;

/**
 * @author jachym.bartik
 */
public class DynamicRecordName extends RecordName {
    
    private final Signature signature;
    
    public Signature signature() {
        return signature;
    }
    
    public DynamicRecordName(String value, Signature signature) {
        super(value);
        this.signature = signature;
    }
    
    @Override
    public boolean equals(Object object) {
        return object instanceof DynamicRecordName dynamicName
            && value.equals(dynamicName.value)
            && signature.equals(dynamicName.signature);
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }
}
