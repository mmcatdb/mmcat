package cz.cuni.matfyz.core.serialization;

/**
 * @author jachymb.bartik
 */
public interface Identified<T extends Comparable<T>> {
    
    public T identifier();

}
