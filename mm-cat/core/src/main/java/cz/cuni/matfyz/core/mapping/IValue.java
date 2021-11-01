package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;
import java.util.Collection;
import org.javatuples.Pair;

/**
 * A value is either simple (a signature of a morphism that maps property object to the value object) or complex (set of another properties i.e. complex property).
 * Its implemented by {@link SimpleValue} or {@link ComplexProperty}.
 * @author pavel.koupil, jachym.bartik
 */
public interface IValue
{
    /**
     * Part of the value-dependent logic of the {@link ComplexProperty#process() } function
     * @param context
     * @return 
     */
    public Collection<Pair<Signature, ComplexProperty>> process(Context context);
}
