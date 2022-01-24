package cz.cuni.matfyz.core.record;

import cz.cuni.matfyz.core.category.Signature;

/**
 *
 * @author jachymb.bartik
 */
public class SimpleValueRecord<DataType> extends SimpleRecord<DataType>
{
    private final DataType value;
    
    SimpleValueRecord(RecordName name, Signature signature, DataType value)
    {
        super(name, signature);
        this.value = value;
    }
	
    public DataType getValue()
    {
        return value;
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(": \"").append(value).append("\"");
        
        return builder.toString();
    }
}
