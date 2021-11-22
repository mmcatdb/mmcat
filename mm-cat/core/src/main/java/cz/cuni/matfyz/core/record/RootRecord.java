package cz.cuni.matfyz.core.record;

import cz.cuni.matfyz.core.category.Signature;

import java.util.*;

/**
 * This class represents a root of the record tree.
 * @author jachymb.bartik
 */
public class RootRecord extends ComplexRecord
{
    //private final Map<Signature, SimpleRecord> quickAccess = new TreeMap<>();
    
	public RootRecord()
    {
        super(null, null, null);
	}
    
    @Override
    protected RootRecord root()
    {
        return this;
    }
    
    /*
    protected void register(SimpleRecord record)
    {
        quickAccess.put(record.signature(), record);
    }
    
    public String findValue(Signature signature)
    {
        SimpleRecord simpleRecord = quickAccess.get(signature);
        
        // This is a workaround, the generic method won't work because java has probably the second worst generics implementation (after python)
        // if (simpleRecord instanceof SimpleRecord<DataType> data)
        
        return simpleRecord.getValue() instanceof String string ? string : null;
    }
    */
}
