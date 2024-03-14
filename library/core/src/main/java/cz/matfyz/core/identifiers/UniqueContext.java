package cz.matfyz.core.identifiers;

import java.util.Collection;

/**
 * @author jachymb.bartik
 */
public interface UniqueContext<O extends Identified<O, K>, K extends Comparable<K>> {

    O createUniqueObject(O object);

    void deleteUniqueObject(K id);

    void deleteUniqueObject(O object);

    O getUniqueObject(K id);

    Collection<O> getAllUniqueObjects();

}
