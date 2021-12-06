package cz.cuni.matfyz.core.record;

/**
 * This class represents a general node of the record tree. Record was already taken by java ...
 * @author jachymb.bartik
 */
public abstract class DataRecord
{
    protected final Name name;
    private final DataRecord parent;
    
	protected DataRecord(Name name, DataRecord parent)
    {
		this.name = name;
        this.parent = parent;
	}
    
    public Name getName()
    {
        return this.name;
    }
    
    public DataRecord getParent()
    {
        return this.parent;
    }
    
    // Iterate through all simple properties of this tree
    // public abstract Set<DataRecord> records();
}
