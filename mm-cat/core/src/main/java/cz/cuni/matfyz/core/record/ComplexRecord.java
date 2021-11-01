package cz.cuni.matfyz.core.record;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * This class represents an inner node of the record tree.
 * The value of this record are its children.
 * @author jachymb.bartik
 */
public class ComplexRecord extends Record
{
    private final List<Record> children = new ArrayList<>();
    
	protected ComplexRecord(Name name, ComplexRecord parent)
    {
		super(name, parent);
	}
    
    public List<Record> getChildren()
    {
        return children;
    }
    
    public ComplexRecord addComplexRecord(Name name)
    {
        ComplexRecord record = new ComplexRecord(name, this);
        children.add(record);
        return record;
    }
    
    public <DataType> SimpleRecord<DataType> addSimpleRecord(Name name, DataType value)
    {
        SimpleRecord record = new SimpleRecord(name, value, this);
        children.add(record);
        return record;
    }
    
    @Override
    public Set<Record> records()
    {
        Set output = Set.of(this);
        children.stream().map(child -> child.records())
            .forEach(set -> output.addAll(set));
        
        return output;
    }
}
