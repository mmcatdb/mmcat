package cz.cuni.matfyz.core.record;

/**
 *
 * This class represents a general node of the record tree.
 */
public class Property
{
    private final String name;
    private final ComplexProperty parent;
    
	protected Property(String name, ComplexProperty parent)
    {
		this.name = name;
        this.parent = parent;
	}
    
    public String getName()
    {
        return this.name;
    }
    
    public ComplexProperty getParent()
    {
        return this.parent;
    }
}
