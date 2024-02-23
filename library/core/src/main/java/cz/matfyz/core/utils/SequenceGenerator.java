package cz.matfyz.core.utils;

import java.util.Set;
import java.util.TreeSet;

public class SequenceGenerator {

    private int next;
    private final Set<Integer> used = new TreeSet<>();

    public SequenceGenerator(int initial) {
        this.next = initial;
    }

    public int next() {
        while (used.contains(next))
            next++;

        used.add(next);
        return next;
    }

    public int next(int value) {
        if (next == value)
            throw new IllegalArgumentException("Can't generate sequence. Value " + value + " is already used.");

        used.add(value);
        return value;
    }

    public void set(int initial) {
        next = initial;
    }

    public void reset(int initial) {
        next = initial;
        used.clear();
    }

}
