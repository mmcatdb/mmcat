package cz.cuni.matfyz.core.serialization;

/**
 *
 * @author jachymb.bartik
 */
public interface UniqueContext<ObjectType extends Identified<IdType>, IdType extends Comparable<IdType>> {
    
    public ObjectType createUniqueObject(ObjectType object);

    public ObjectType getUniqueObject(IdType id);

    public Iterable<ObjectType> getAllUniqueObjects();

}
