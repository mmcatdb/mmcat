package cz.cuni.matfyz.core.record;

import java.util.Set;

/**
 * This class represents a general node of the record tree. Record was already taken by java ...
 * @author jachymb.bartik
 */
public abstract class DataRecord
{
    private final Name name;
    private final DataRecord parent;
    private final RootRecord root;
    
    protected RootRecord root()
    {
        return root;
    }
    
	protected DataRecord(Name name, DataRecord parent, RootRecord root)
    {
		this.name = name;
        this.parent = parent;
        this.root = root;
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
