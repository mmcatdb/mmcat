package cz.cuni.matfyz.core.record;

import cz.cuni.matfyz.core.category.Signature;

import java.util.Set;

/**
 * Simple property cannot have children so it is a leaf node in the record tree.
 * However, it can have value.
 * @author jachymb.bartik
 * @param <DataType> a type of the value of this property.
 */
public class SimpleRecord<DataType> extends DataRecord
{
	private final DataType value;
    private final Signature signature;
    
    SimpleRecord(Name name, ComplexRecord parent, RootRecord root, DataType value, Signature signature)
    {
        super(name, parent, root);
        this.value = value;
        this.signature = signature;
    }
	
    public DataType getValue()
    {
        return value;
    }
    
    public Signature signature()
    {
        return signature;
    }
    
    /*
    @Override
    public Set<DataRecord> records()
    {
        return Set.of(this);
    }
    */
}
