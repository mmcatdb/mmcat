package cz.cuni.matfyz.core.serialization;

/**
 *
 * @author jachymb.bartik
 */
public interface Identified<IdType extends Comparable<IdType>> {
    
    public IdType identifier();

    //public UniqueContext<? extends Identified<IdType>, IdType> uniqueContext();

}
