package cz.matfyz.inference.schemaconversion.utils;

/**
 * A class that generates unique sequential numbers starting from a specified initial value.
 */
public class UniqueNumberGenerator {

    private int nextValue;

    public UniqueNumberGenerator(int startValue) {
        this.nextValue = startValue;
    }

    /**
     * Returns the next unique number in the sequence. This method is synchronized to ensure
     * thread safety, so each call to {@code next()} returns a unique value even in a multithreaded environment.
     */
    public synchronized int next() {
        return nextValue++;
    }

}
