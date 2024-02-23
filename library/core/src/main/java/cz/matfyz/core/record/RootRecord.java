package cz.matfyz.core.record;

/**
 * This class represents a root of the record tree.
 * @author jachymb.bartik
 */
public class RootRecord extends ComplexRecord {

    //private final Map<Signature, SimpleRecord> quickAccess = new TreeMap<>();

    public RootRecord() {
        super(null);
    }

    /*
    protected void register(SimpleRecord record) {
        quickAccess.put(record.signature(), record);
    }

    public String findValue(Signature signature) {
        SimpleRecord simpleRecord = quickAccess.get(signature);

        // This is a workaround, the generic method won't work because java has probably the second worst generics implementation (after python)
        // if (simpleRecord instanceof SimpleRecord<DataType> data)

        return simpleRecord.getValue() instanceof String string ? string : null;
    }
    */
}
