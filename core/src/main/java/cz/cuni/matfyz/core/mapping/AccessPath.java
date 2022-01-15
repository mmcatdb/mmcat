package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;

/**
 * Common ancestor for the access path tree. It can be a {@link ComplexProperty} or a {@link SimpleProperty}.
 * Each node is a tuple (name, context, value).
 * @author pavel.koupil, jachym.bartik
 */
public abstract class AccessPath
{
    protected final Name name;
    
    public Name name()
    {
        return name;
    }
    
    public abstract IContext context();
    
    public abstract IValue value();
    
    protected AccessPath(Name name)
    {
        this.name = name;
    }
    
    protected abstract boolean hasSignature(Signature signature);
    
    public boolean equals(AccessPath path)
    {
        return name.equals(path.name);
    }
    
    public abstract Signature signature();
}
