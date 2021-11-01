package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;
import java.util.*;
import org.javatuples.Pair;

/**
 * A simple value is a signature of morphism ?(which maps the parent property to this value)?
 * @author jachymb.bartik
 */
public class SimpleValue implements IValue
{
    private final Signature signature;
    
    public Signature getSignature()
    {
        return signature;
    }
    
    public SimpleValue(Signature signature)
    {
        this.signature = signature;
    }
    
    @Override
    public Collection<Pair<Signature, ComplexProperty>> process(Context context)
    {
        final Signature newSignature = Signature.combine(context.getSignature(), getSignature());
        return List.of(new Pair(newSignature, null));
    }
}
