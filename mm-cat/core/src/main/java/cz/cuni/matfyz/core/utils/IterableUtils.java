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
        
        while(iteratorA.hasNext() && iteratorB.hasNext())
            action.accept(iteratorA.next(), iteratorB.next());
        
        return iteratorA.hasNext() || iteratorB.hasNext();
    }
}
