package cz.cuni.matfyz.core.record;

import cz.cuni.matfyz.core.category.Signature;

import java.util.*;

/**
 *
 * @author jachymb.bartik
 */
public class SimpleArrayRecord<DataType> extends SimpleRecord<DataType>
{
    private final List<DataType> values;
    
    SimpleArrayRecord(RecordName name, Signature signature, List<DataType> values)
    {
        super(name, signature);
        this.values = values;
    }
	
    public List<DataType> getValues()
    {
        return values;
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(": [");
        if (values.size() > 0)
            builder.append("\"").append(values.get(0)).append("\"");
        for (int i = 1; i < values.size(); i++)
            builder.append(", \"").append(values.get(i)).append("\"");
        builder.append("]");
        
        return builder.toString();
    }
}
