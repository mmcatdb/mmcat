package cz.matfyz.core.identification;

/**
 * @author jachymb.bartik
 */
public interface Identified<T extends Comparable<T>> {

    T identifier();

}
