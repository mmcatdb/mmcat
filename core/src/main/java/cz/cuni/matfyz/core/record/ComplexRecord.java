package cz.cuni.matfyz.core.record;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.utils.*;

import java.util.*;

/**
 * This class represents an inner node of the record tree.
 * The value of this record are its children.
 * @author jachymb.bartik
 */
public class ComplexRecord extends DataRecord implements IComplexRecord
{
    //private final List<DataRecord> children = new ArrayList<>();
    //private final Map<Signature, Set<DataRecord>> children = new TreeMap<>();
    
    private final Map<Signature, List<ComplexRecord>> children = new TreeMap<>();
    private final List<ComplexRecord> dynamicChildren = new ArrayList<>();
    private Signature dynamicSignature;
    private final Map<Signature, SimpleRecord<?>> values = new TreeMap<>();
    private final List<SimpleValueRecord<?>> dynamicValues = new ArrayList<>();
    //private SimpleValueRecord<?> firstDynamicValue = null;
    
	protected ComplexRecord(RecordName name)
    {
		super(name);
	}
    
    /*
    public Map<Signature, List<ComplexRecord>> children()
    {
        return children;
    }
    */

    public boolean hasComplexRecords(Signature signature)
    {
        //return children.containsKey(signature) || (signature.equals(dynamicSignature));
        return children.containsKey(signature);
    }

    public List<? extends IComplexRecord> getComplexRecords(Signature signature)
    {
        //return signature.equals(dynamicSignature) ? dynamicChildren : children.get(signature);
        return children.get(signature);
    }
    
    public boolean hasDynamicChildren()
    {
        return dynamicChildren.size() > 0;
    }
    
    public List<? extends IComplexRecord> getDynamicChildren()
    {
        return dynamicChildren;
    }

    public Signature dynamicSignature()
    {
        return dynamicSignature;
    }

    public boolean hasSimpleRecord(Signature signature)
    {
        return values.containsKey(signature);
    }

    public SimpleRecord<?> getSimpleRecord(Signature signature)
    {
        return values.get(signature);
    }

    public boolean hasDynamicValues()
    {
        return dynamicValues.size() > 0;
    }
    
    public List<SimpleValueRecord<?>> getDynamicValues()
    {
        return dynamicValues;
    }
    
    public boolean containsDynamicValue(Signature signature)
    {
        if (dynamicValues.size() == 0)
            return false;

        SimpleValueRecord<?> firstDynamicValue = dynamicValues.get(0);
        return signature.equals(firstDynamicValue.signature) ||
            firstDynamicValue.name() instanceof DynamicRecordName dynamicName && signature.equals(dynamicName.signature());
    }
    
    public ComplexRecord addComplexRecord(RecordName name, Signature signature)
    {
        ComplexRecord record = new ComplexRecord(name);
        
        if (record.name instanceof StaticRecordName)
        {
            List<ComplexRecord> childSet = children.get(signature);
            if (childSet == null)
            {
                childSet = new ArrayList<>();
                children.put(signature, childSet);
            }
            
            childSet.add(record);
        }
        else
        {
            dynamicChildren.add(record);
            dynamicSignature = signature;
        }
        
        return record;
    }
    
    public <DataType> SimpleArrayRecord<DataType> addSimpleArrayRecord(RecordName name, Signature signature, List<DataType> values)
    {
        var record = new SimpleArrayRecord<>(name, signature, values);
        this.values.put(signature, record);
        
        return record;
    }
    
    public <DataType> SimpleValueRecord<DataType> addSimpleValueRecord(RecordName name, Signature signature, DataType value)
    {
        var record = new SimpleValueRecord<>(name, signature, value);

        if (name instanceof StaticRecordName)
            values.put(signature, record);
        else if (name instanceof DynamicRecordName dynamicName)
        {
            /*
            if (firstDynamicValue == null)
                firstDynamicValue = record;
            else
            {
                assert firstDynamicValue.name() instanceof DynamicRecordName firstDynamicName && dynamicName.signature().equals(firstDynamicName.signature()) : "Trying to add a dynamic name with different name signature";
                assert signature.equals(firstDynamicValue.signature()) : "Trying to add a dynamic name with different value signature";
            }
            */
            
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
            
            if (list.size() > 1)
            {
                childrenBuilder.append(firstItem.name).append(": ");
                childrenBuilder.append("[\n");
            
                var innerBuilder = new IntendedStringBuilder(1);
                innerBuilder.append(firstItem);
                for (int i = 1; i < list.size(); i++)
                    innerBuilder.append(",\n").append(list.get(i));

                childrenBuilder.append(innerBuilder);
                if (list.size() > 1)
                    childrenBuilder.append("]");
            }
            // Normal complex property
            else
            {
                childrenBuilder.append(firstItem.name).append(": ").append(firstItem);
            }
            
            childrenBuilder.append(",\n");
        }

        for (ComplexRecord dynamicChild : dynamicChildren)
        {
            childrenBuilder.append(dynamicChild.name).append(": ").append(dynamicChild).append(",\n");
        }

        String childrenResult = childrenBuilder.toString();
        if (childrenResult.length() > 0)
            childrenResult = childrenResult.substring(0, childrenResult.length() - 2);
        
        builder.append(childrenResult);
        builder.append("\n}");
        
        return builder.toString();
    }
}
