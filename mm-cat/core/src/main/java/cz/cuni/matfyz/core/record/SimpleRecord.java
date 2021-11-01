package cz.cuni.matfyz.core.record;

import java.util.Set;

/**
 * Simple property cannot have children so it is a leaf node in the record tree.
 * However, it can have value.
 * @author jachymb.bartik
 * @param <DataType> a type of the value of this property.
 */
public class SimpleRecord<DataType> extends Record
{
	private final DataType value;
    
    SimpleRecord(Name name, DataType value, ComplexRecord parent)
    {
        super(name, parent);
        this.value = value;
    }
	
    public DataType getValue()
    {
        return value;
    }
    
    @Override
    public Set<Record> records()
    {
        return Set.of(this);
    }
}
