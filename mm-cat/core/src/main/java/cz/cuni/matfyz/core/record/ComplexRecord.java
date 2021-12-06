package cz.cuni.matfyz.core.record;

import cz.cuni.matfyz.core.category.Signature;

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
    
    public <DataType> SimpleRecord<DataType> addSimpleRecord(Name name, DataType value, Signature signature)
    {
        SimpleRecord record = new SimpleRecord(name, this, value, signature);
        values.put(signature, record);
        
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
        StringBuilder childrenBuilder = new StringBuilder();
        for (Signature signature : children.keySet())
            childrenBuilder.append(children.get(signature));
        String childrenResult = childrenBuilder.toString();
        
        StringBuilder builder = new StringBuilder();
        builder.append("Name: ").append(name).append("\n")
            .append("Simple values:\n");
        for (Signature signature : values.keySet())
            builder.append("\t").append(values.get(signature));
        
        builder.append("Complex values:\n");
        for (String line : childrenResult.lines().toList())
            builder.append("\t").append(line).append("\n");
        
        return builder.toString();
    }
}
