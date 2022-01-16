package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;

/**
 * A simple value node in the access path tree. Its context is undefined (null).
 * @author jachymb.bartik
 */
public class SimpleProperty extends AccessPath
{
    @Override
    public IContext context()
    {
        return null;
    }
    
    private final SimpleValue value;
    
    @Override
    public SimpleValue value()
    {
        return value;
    }
    
    public SimpleProperty(Name name, SimpleValue value)
    {
        super(name);
        
        this.value = value;
    }
    
    public SimpleProperty(String name, Signature value)
    {
        this(new StaticName(name), new SimpleValue(value));
    }
    
    public SimpleProperty(Signature name, Signature value)
    {
        this(new DynamicName(name), new SimpleValue(value));
    }
    
    @Override
    protected boolean hasSignature(Signature signature)
    {
        if (signature == null)
            return value.signature().getType() == Signature.Type.EMPTY;
        
        return value.signature().equals(signature);
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(": ").append(value);
        
        return builder.toString();
    }
    
    @Override
    public Signature signature()
    {
        return value.signature();
    }
}
