package cz.matfyz.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class ArrayUtils {

    private ArrayUtils() {}

    @SafeVarargs
    public static <T> T[] concatenate(T[]... arrays) {
        int length = 0;
        for (T[] array : arrays)
            length += array.length;

        @SuppressWarnings("unchecked")
        final T[] output = (T[]) new Object[length];

        int startIndex = 0;
        for (T[] array : arrays) {
            System.arraycopy(array, 0, output, startIndex, array.length);
            startIndex += array.length;
        }

        return output;
    }

    /**
     * Why java, why you have to be like this?
     * @param arrays The arrays we want to concatenate
     * @return The concatenated array (basically a flatMap)
     */
    public static int[] concatenate(int[]... arrays) {
        return concatenate(Arrays.asList(arrays));
    }

    public static int[] concatenate(List<int[]> arrays) {
        int length = 0;
        for (int[] array : arrays)
            length += array.length;

        final int[] output = new int[length];

        int startIndex = 0;
        for (int[] array : arrays) {
            System.arraycopy(array, 0, output, startIndex, array.length);
            startIndex += array.length;
        }

        return output;
    }

    /**
     *
     * @param <T> Comparable type
     * @param source Sorted objects
     * @param filter Also sorted objects
     * @return A copy of source array without all the objects that are in the filter array
     */
    public static <T extends Comparable<T>> List<T> filterSorted(Iterable<T> source, Iterable<T> filter) {
        final var output = new ArrayList<T>();

        final var sourceIterator = source.iterator();
        final var filterIterator = filter.iterator();

        T sourceObject = sourceIterator.hasNext() ? sourceIterator.next() : null;
        T filterObject = filterIterator.hasNext() ? filterIterator.next() : null;

        while (sourceObject != null && filterObject != null) {
            final var comparison = sourceObject.compareTo(filterObject);

            if (comparison < 0) {
                output.add(sourceObject);
                sourceObject = sourceIterator.hasNext() ? sourceIterator.next() : null;
            }
            else if (comparison > 0) {
                filterObject = filterIterator.hasNext() ? filterIterator.next() : null;
            }
            else {
                sourceObject = sourceIterator.hasNext() ? sourceIterator.next() : null;
                filterObject = filterIterator.hasNext() ? filterIterator.next() : null;
            }
        }

        if (sourceObject != null) {
            output.add(sourceObject);

            while (sourceIterator.hasNext())
               output.add(sourceIterator.next());
        }

        return output;
    }

    public static <T> int indexOf(Iterable<T> source, Predicate<T> predicate) {
        int index = 0;
        for (final T object : source) {
            if (predicate.test(object))
                return index;

            index++;
        }

        return -1;
    }

    public static <T> boolean isShallowEqual(List<T> a, List<T> b) {
        if (a.size() != b.size())
            return false;

        for (int i = 0; i < a.size(); i++) {
            if (a.get(i) != b.get(i))
                return false;
        }

        return true;
    }

}
