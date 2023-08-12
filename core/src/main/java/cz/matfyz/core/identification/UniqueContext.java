package cz.matfyz.core.identification;

import java.util.Collection;

/**
 * @author jachymb.bartik
 */
public interface UniqueContext<O extends Identified<I>, I extends Comparable<I>> {
    
    public O createUniqueObject(O object);

    public void deleteUniqueObject(I id);

    public void deleteUniqueObject(O object);

    public O getUniqueObject(I id);

    public Collection<O> getAllUniqueObjects();

}
