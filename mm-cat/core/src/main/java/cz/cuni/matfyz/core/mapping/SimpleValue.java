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
    
    public SimpleValue Empty()
    {
        return new SimpleValue(Signature.Empty());
    }
}
