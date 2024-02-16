package cz.matfyz.core.utils;

/**
 * @author pavel.koupil, jachym.bartik
 * @param <T>
 * @param <U>
 */
public class ComparablePair<T extends Comparable<T>, U extends Comparable<U>> implements Comparable<ComparablePair<T, U>> {
    
    private final T value1;
    private final U value2;

    public T getValue1() {
        return value1;
    }

    public U getValue2() {
        return value2;
    }

    public ComparablePair(T value1, U value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    @Override public int compareTo(ComparablePair<T, U> object) {
        int firstResult = value1.compareTo(object.value1);
        return firstResult != 0 ? firstResult : value2.compareTo(value2);
    }
}
