package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;

/**
 * A simple value node in the access path tree. Its context is undefined (null).
 * @author jachymb.bartik
 */
public class SimpleProperty extends AccessPath
{
    @Override
    public Context getContext()
    {
        return null;
    }
    
    private final SimpleValue value;
    
    @Override
    public SimpleValue getValue()
    {
        return value;
    }
    
    public SimpleProperty(Name name, SimpleValue value)
    {
        super(name);
        
        this.value = value;
    }
    
    @Override
    protected boolean hasSignature(Signature signature)
    {
        if (signature == null)
            return value.getSignature().getType() == Signature.Type.EMPTY;
        
        return value.getSignature().equals(signature);
    }
}
