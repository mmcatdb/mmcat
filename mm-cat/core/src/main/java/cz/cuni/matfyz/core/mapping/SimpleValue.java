package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;

/**
 * A simple value is a signature of morphism ?(which maps the parent property to this value)?
 * @author jachymb.bartik
 */
public class SimpleValue implements IValue
{
    private final Signature signature;
    
    public Signature signature()
    {
        return signature;
    }
    
    public SimpleValue(Signature signature)
    {
        this.signature = signature;
    }
    
    private final static SimpleValue empty = new SimpleValue(Signature.Empty());
    
    public static SimpleValue Empty()
    {
        return empty;
    }
    
    @Override
	public String toString()
    {
        return signature.toString();
	}
}
