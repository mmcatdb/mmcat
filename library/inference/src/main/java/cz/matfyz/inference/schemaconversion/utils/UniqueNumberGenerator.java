package cz.matfyz.inference.schemaconversion.utils;

public class UniqueNumberGenerator {
    private int nextValue;

    public UniqueNumberGenerator(int startValue) {
        this.nextValue = startValue;
    }

    public synchronized int next() {
        return nextValue++;
    }

}
