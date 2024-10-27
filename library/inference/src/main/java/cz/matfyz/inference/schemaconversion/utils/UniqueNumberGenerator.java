package cz.matfyz.inference.schemaconversion.utils;

/**
 * A class that generates unique sequential numbers starting from a specified initial value.
 */
public class UniqueNumberGenerator {

    private int nextValue;

    /**
     * Constructs a new {@code UniqueNumberGenerator} starting from the specified initial value.
     *
     * @param startValue The initial value from which to start generating unique numbers.
     */
    public UniqueNumberGenerator(int startValue) {
        this.nextValue = startValue;
    }

    /**
     * Returns the next unique number in the sequence. This method is synchronized to ensure
     * thread safety, so each call to {@code next()} returns a unique value even in a multithreaded environment.
     *
     * @return The next unique number.
     */
    public synchronized int next() {
        return nextValue++;
    }

}
