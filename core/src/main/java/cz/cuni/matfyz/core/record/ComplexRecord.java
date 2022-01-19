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
    private final Map<Signature, SimpleRecord<?>> values = new TreeMap<>();
    private final List<SimpleValueRecord<?>> dynamicValues = new ArrayList<>();
    private SimpleValueRecord<?> firstDynamicValue = null;
    
	protected ComplexRecord(RecordName name, ComplexRecord parent)
    {
		super(name, parent);
	}
    
    /*
    public Map<Signature, List<ComplexRecord>> children()
    {
        return children;
    }
    */

    public boolean hasComplexRecords(Signature signature)
    {
        return children.containsKey(signature);
    }

    public List<ComplexRecord> getComplexRecords(Signature signature)
    {
        return children.get(signature);
    }
    
    /*
    public Map<Signature, SimpleRecord<?>> values()
    {
        return values;
    }
    */

    public boolean hasSimpleRecord(Signature signature)
    {
        return values.containsKey(signature);
    }

    public SimpleRecord<?> getSimpleRecord(Signature signature)
    {
        return values.get(signature);
    }
    
    public List<SimpleValueRecord<?>> dynamicValues()
    {
        return dynamicValues;
    }
    
    public boolean hasDynamicValues()
    {
        return firstDynamicValue != null;
    }
    
    public boolean containsDynamicSignature(Signature signature)
    {
        if (firstDynamicValue == null)
            return false;

        return signature.equals(firstDynamicValue.signature) ||
            firstDynamicValue.name() instanceof DynamicRecordName dynamicName && signature.equals(dynamicName.signature());
    }
    
    public ComplexRecord addComplexRecord(RecordName name, Signature signature)
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
    
    public <DataType> SimpleArrayRecord<DataType> addSimpleArrayRecord(RecordName name, Signature signature, List<DataType> values)
    {
        var record = new SimpleArrayRecord<>(name, this, signature, values);
        this.values.put(signature, record);
        
        return record;
    }
    
    public <DataType> SimpleValueRecord<DataType> addSimpleValueRecord(RecordName name, Signature signature, DataType value)
    {
        var record = new SimpleValueRecord<>(name, this, signature, value);

        if (name instanceof StaticRecordName)
            values.put(signature, record);
        else if (name instanceof DynamicRecordName dynamicName)
        {
            if (firstDynamicValue == null)
                firstDynamicValue = record;
            else
            {
                assert firstDynamicValue.name() instanceof DynamicRecordName firstDynamicName && dynamicName.signature().equals(firstDynamicName.signature()) : "Trying to add a dynamic name with different name signature";
                assert signature.equals(firstDynamicValue.signature()) : "Trying to add a dynamic name with different value signature";
            }
            
            dynamicValues.add(record);
        }

        
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
        
        for (SimpleValueRecord<?> dynamicValue : dynamicValues)
            childrenBuilder.append(dynamicValue).append(",\n");
        
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
