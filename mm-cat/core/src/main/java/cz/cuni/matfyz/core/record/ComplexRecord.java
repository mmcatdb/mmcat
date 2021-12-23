package cz.cuni.matfyz.core.record;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.utils.*;

import java.util.*;

/**
 * This class represents an inner node of the record tree.
 * The value of this record are its children.
 * @author jachymb.bartik
 */
public class ComplexRecord extends DataRecord
{
    //private final List<DataRecord> children = new ArrayList<>();
    //private final Map<Signature, Set<DataRecord>> children = new TreeMap<>();
    
    private final Map<Signature, List<ComplexRecord>> children = new TreeMap<>();
    private final Map<Signature, SimpleRecord> values = new TreeMap<>();
    
	protected ComplexRecord(Name name, ComplexRecord parent)
    {
		super(name, parent);
	}
    
    public Map<Signature, List<ComplexRecord>> children()
    {
        return children;
    }
    
    public Map<Signature, SimpleRecord> values()
    {
        return values;
    }
    
    public ComplexRecord addComplexRecord(Name name, Signature signature)
    {
        ComplexRecord record = new ComplexRecord(name, this);
        
        List<ComplexRecord> childSet = children.get(signature);
        if (childSet == null)
        {
            childSet = new ArrayList<>();
            children.put(signature, childSet);
        }
        
        childSet.add(record);
        
        return record;
    }
    
    public <DataType> SimpleRecord<DataType> addSimpleValueRecord(Name name, Signature signature, DataType value)
    {
        SimpleRecord record = new SimpleValueRecord(name, this, signature, value);
        values.put(signature, record);
        
        return record;
    }
    
    public <DataType> SimpleRecord<DataType> addSimpleArrayRecord(Name name, Signature signature, List<DataType> values)
    {
        SimpleRecord record = new SimpleArrayRecord(name, this, signature, values);
        this.values.put(signature, record);
        
        return record;
    }
    
    /*
    @Override
    Set<DataRecord> records()
    {
        Set output = Set.of(this);
        children.values().stream().forEach(set -> output.addAll(set));
        
        return output;
    }
    */
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        
        var childrenBuilder = new IntendedStringBuilder(1);
        for (Signature signature : values.keySet())
            childrenBuilder.append(values.get(signature)).append(",\n");
        for (Signature signature : children.keySet())
        {
            List<ComplexRecord> list = children.get(signature);
            ComplexRecord firstItem = list.get(0);
            
            childrenBuilder.append(firstItem.name).append(": ");
            if (list.size() > 1)
            {
                childrenBuilder.append("[\n");
            
                var innerBuilder = new IntendedStringBuilder(1);
                innerBuilder.append(firstItem);
                for (int i = 1; i < list.size(); i++)
                    innerBuilder.append(",\n").append(list.get(i));

                childrenBuilder.append(innerBuilder);
                if (list.size() > 1)
                    childrenBuilder.append("]");
            }
            else
            {
                childrenBuilder.append(firstItem);
            }
            
            childrenBuilder.append(",\n");
        }
        String childrenResult = childrenBuilder.toString();
        childrenResult = childrenResult.substring(0, childrenResult.length() - 2);
        
        builder.append(childrenResult);
        builder.append("\n}");
        
        return builder.toString();
    }
}
