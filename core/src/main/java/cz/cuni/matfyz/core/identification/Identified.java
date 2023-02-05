package cz.cuni.matfyz.core.identification;

/**
 * @author jachymb.bartik
 */
public interface Identified<T extends Comparable<T>> {
    
    public T identifier();

}
