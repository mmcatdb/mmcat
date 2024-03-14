package cz.matfyz.core.identifiers;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author jachymb.bartik
 */
public class MapUniqueContext<O extends Identified<O, K>, K extends Comparable<K>> implements UniqueContext<O, K> {

    private final Map<K, O> uniqueObjects;

    public MapUniqueContext() {
        uniqueObjects = new TreeMap<>();
    }

    public MapUniqueContext(Collection<O> collection) {
        uniqueObjects = new TreeMap<>();
        for (O object : collection)
            createUniqueObject(object);
    }

    @Override public O createUniqueObject(O object) {
        if (!uniqueObjects.containsKey(object.identifier()))
            uniqueObjects.put(object.identifier(), object);

        return uniqueObjects.get(object.identifier());
    }

    @Override public void deleteUniqueObject(K id) {
        uniqueObjects.remove(id);
    }

    @Override public void deleteUniqueObject(O object) {
        uniqueObjects.remove(object.identifier());
    }

    @Override public O getUniqueObject(K id) {
        return uniqueObjects.get(id);
    }

    @Override public Collection<O> getAllUniqueObjects() {
        return uniqueObjects.values();
    }

}
