package cz.cuni.matfyz.core.record;

import java.util.Set;

/**
 * This class represents a general node of the record tree.
 * @author jachymb.bartik
 */
public abstract class Record
{
    private final Name name;
    private final Record parent;
    
	protected Record(Name name, Record parent)
    {
		this.name = name;
        this.parent = parent;
	}
    
    public Name getName()
    {
        return this.name;
    }
    
    public Record getParent()
    {
        return this.parent;
    }
    
    // Iterate through all simple properties of this tree
    public abstract Set<Record> records();
}
