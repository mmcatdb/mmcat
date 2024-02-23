package cz.matfyz.core.identification;

import java.util.Collection;

/**
 * @author jachymb.bartik
 */
public interface UniqueContext<O extends Identified<I>, I extends Comparable<I>> {

    O createUniqueObject(O object);

    void deleteUniqueObject(I id);

    void deleteUniqueObject(O object);

    O getUniqueObject(I id);

    Collection<O> getAllUniqueObjects();

}
