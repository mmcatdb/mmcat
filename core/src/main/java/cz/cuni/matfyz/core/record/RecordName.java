package cz.cuni.matfyz.core.record;

/**
 * @author jachym.bartik
 */
public abstract class RecordName {
    
    protected final String value;
    
    public String value() {
        return value;
    }

    protected RecordName(String value) {
        this.value = value;
    }
    
    /*
    @Override
    public int compareTo(RecordName name) {
        return value.compareTo(name.value);
    }
    */
}
