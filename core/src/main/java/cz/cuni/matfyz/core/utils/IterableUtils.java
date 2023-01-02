package cz.cuni.matfyz.core.utils;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * @author jachymb.bartik
 */
public class IterableUtils {

    private IterableUtils() {}

    /**
     * Iterate through two iterables and call action on values with same indices. Returns if both of the iterables have same length.
     * @param <T1>
     * @param <T2>
     * @param a
     * @param b
     * @param action
     * @return
     */
    public static <T1, T2> boolean iterateTwo(Iterable<T1> a, Iterable<T2> b, BiConsumer<T1, T2> action) {
        Iterator<T1> iteratorA = a.iterator();
        Iterator<T2> iteratorB = b.iterator();
        
        while (iteratorA.hasNext() && iteratorB.hasNext())
            action.accept(iteratorA.next(), iteratorB.next());
        
        return !iteratorA.hasNext() && !iteratorB.hasNext();
    }
    
    /**
     * Iterate through two iterables and call function on values with same indices. If the function returns false, the iteration is stopped.
     * Returns if both of the iterables have same length and if the function didn't return false.
     * @param <T1>
     * @param <T2>
     * @param a
     * @param b
     * @param continueFunction
     * @return
     */
    public static <T1, T2> boolean iterateTwo(Iterable<T1> a, Iterable<T2> b, BiPredicate<T1, T2> continueFunction) {
        Iterator<T1> iteratorA = a.iterator();
        Iterator<T2> iteratorB = b.iterator();
        
        boolean result = true;
        while (iteratorA.hasNext() && iteratorB.hasNext())
            if (!continueFunction.test(iteratorA.next(), iteratorB.next())) {
                result = false;
                break;
            }
        
        return result && !iteratorA.hasNext() && !iteratorB.hasNext();
    }
}
