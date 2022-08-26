package cz.cuni.matfyz.core.serialization;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author jachymb.bartik
 */
public interface UniqueContext<O extends Identified<I>, I extends Comparable<I>> extends Serializable {
    
    public O createUniqueObject(O object);

    public void deleteUniqueObject(O object);

    public O getUniqueObject(I id);

    public Collection<O> getAllUniqueObjects();

}
