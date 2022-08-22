package cz.cuni.matfyz.core.serialization;

import java.util.Collection;

/**
 *
 * @author jachymb.bartik
 */
public interface UniqueContext<ObjectType extends Identified<IdType>, IdType extends Comparable<IdType>> {
    
    public ObjectType createUniqueObject(ObjectType object);

    public void deleteUniqueObject(ObjectType object);

    public ObjectType getUniqueObject(IdType id);

    public Collection<ObjectType> getAllUniqueObjects();

}
