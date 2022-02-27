package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.utils.JSONConverterBase;
import cz.cuni.matfyz.core.utils.JSONConvertible;
import cz.cuni.matfyz.core.utils.JSONSwitchConverterBase;

import java.util.Set;

/**
 * Common ancestor for the access path tree. It can be a {@link ComplexProperty} or a {@link SimpleProperty}.
 * Each node is a tuple (name, context, value).
 * @author pavel.koupil, jachym.bartik
 */
public abstract class AccessPath implements JSONConvertible
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

    public static class Converter extends JSONSwitchConverterBase<AccessPath> {

        @Override
        protected Set<JSONConverterBase<? extends AccessPath>> getChildConverters() {
            return Set.of(
                new ComplexProperty.Converter(),
                new SimpleProperty.Converter()
            );
        }

    }
}
