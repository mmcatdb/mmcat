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
    private final Map<Signature, Set<ComplexRecord>> children = new TreeMap<>();
    private final Map<Signature, SimpleRecord> values = new TreeMap<>();
    
	protected ComplexRecord(Name name, ComplexRecord parent, RootRecord root)
    {
		super(name, parent, root);
	}
    
    public Map<Signature, Set<ComplexRecord>> children()
    {
        return children;
    }
    
    public Map<Signature, SimpleRecord> values()
    {
        return values;
    }
    
    public ComplexRecord addComplexRecord(Name name, Signature signature)
    {
        ComplexRecord record = new ComplexRecord(name, this, root());
        
        Set<ComplexRecord> childSet = children.get(signature);
        if (childSet == null)
        {
            childSet = new TreeSet<>();
            children.put(signature, childSet);
        }
        
        childSet.add(record);
        
        return record;
    }
    
    public <DataType> SimpleRecord<DataType> addSimpleRecord(Name name, DataType value, Signature signature)
    {
        SimpleRecord record = new SimpleRecord(name, this, root(), value, signature);
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
}
