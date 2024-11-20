package cz.matfyz.core.utils;

import java.util.Collection;

/**
 * A class that generates unique sequential numbers starting from a specified initial value.
 * All methods are synchronized to ensure thread safety, so each call returns a unique value even in a multithreaded environment.
 */
public class UniqueSequentialGenerator {

    private int nextValue;

    private UniqueSequentialGenerator(int nextValue) {
        this.nextValue = nextValue;
    }

    public static UniqueSequentialGenerator create() {
        return new UniqueSequentialGenerator(0);
    }

    public static UniqueSequentialGenerator create(int nextValue) {
        return new UniqueSequentialGenerator(nextValue);
    }

    public static UniqueSequentialGenerator create(Collection<Integer> current) {
        final int max = current.stream().reduce(0, Math::max);
        return new UniqueSequentialGenerator(max + 1);
    }

    public static UniqueSequentialGenerator create(int... current) {
        int max = 0;
        for (final int i : current)
            max = Math.max(max, i);
        return new UniqueSequentialGenerator(max + 1);
    }

    /**
     * Returns the next unique number in the sequence.
     */
    public synchronized int next() {
        return nextValue++;
    }

    /**
     * Returns the next unique number in the sequence casted to string.
     */
    public synchronized String nextString() {
        return "" + next();
    }

    public record SerializedUniqueSequentialGenerator(int nextValue) {}

    public SerializedUniqueSequentialGenerator serialize() {
        return new SerializedUniqueSequentialGenerator(nextValue);
    }

    public static UniqueSequentialGenerator deserialize(SerializedUniqueSequentialGenerator serialized) {
        return new UniqueSequentialGenerator(serialized.nextValue);
    }

}
