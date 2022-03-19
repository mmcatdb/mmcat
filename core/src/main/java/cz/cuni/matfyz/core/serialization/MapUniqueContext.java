package cz.cuni.matfyz.core.serialization;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author jachymb.bartik
 */
public class MapUniqueContext<ObjectType extends Identified<IdType>, IdType extends Comparable<IdType>> implements UniqueContext<ObjectType, IdType> {
    
    private final Map<IdType, ObjectType> uniqueObjects;

    public MapUniqueContext() {
        uniqueObjects = new TreeMap<>();
    }

    public MapUniqueContext(Collection<ObjectType> collection) {
        uniqueObjects = new TreeMap<>();
        for (ObjectType object : collection)
            createUniqueObject(object);
    }

    @Override
    public ObjectType createUniqueObject(ObjectType object) {
        if (!uniqueObjects.containsKey(object.identifier()))
            uniqueObjects.put(object.identifier(), object);
        
        return uniqueObjects.get(object.identifier());
    }

    @Override
    public ObjectType getUniqueObject(IdType id) {
        return uniqueObjects.get(id);
    }

    @Override
    public Collection<ObjectType> getAllUniqueObjects() {
        return uniqueObjects.values();
    }

}
