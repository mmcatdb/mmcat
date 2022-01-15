package cz.cuni.matfyz.core.utils;

import java.util.*;
import java.util.function.*;

/**
 *
 * @author jachymb.bartik
 */
public class IterableUtils
{
    /**
     * Iterate through two iterables and call action on values with same indices. Returns if both of the iterables have same length.
     * @param <TypeA>
     * @param <TypeB>
     * @param a
     * @param b
     * @param action
     * @return 
     */
    public static <TypeA, TypeB> boolean iterateTwo(Iterable<TypeA> a, Iterable<TypeB> b, BiConsumer<TypeA, TypeB> action)
    {
        Iterator<TypeA> iteratorA = a.iterator();
        Iterator<TypeB> iteratorB = b.iterator();
        
        while (iteratorA.hasNext() && iteratorB.hasNext())
            action.accept(iteratorA.next(), iteratorB.next());
        
        return !iteratorA.hasNext() && !iteratorB.hasNext();
    }
    
    /**
     * Iterate through two iterables and call function on values with same indices. If the function returns false, the iteration is stopped.
     * Returns if both of the iterables have same length and if the function didn't return false.
     * @param <TypeA>
     * @param <TypeB>
     * @param a
     * @param b
     * @param continueFunction
     * @return 
     */
    public static <TypeA, TypeB> boolean iterateTwo(Iterable<TypeA> a, Iterable<TypeB> b, BiPredicate<TypeA, TypeB> continueFunction)
    {
        Iterator<TypeA> iteratorA = a.iterator();
        Iterator<TypeB> iteratorB = b.iterator();
        
        boolean result = true;
        while (iteratorA.hasNext() && iteratorB.hasNext())
            if (!continueFunction.test(iteratorA.next(), iteratorB.next()))
            {
                result = false;
                break;
            }
        
        return result && !iteratorA.hasNext() && !iteratorB.hasNext();
    }
}
