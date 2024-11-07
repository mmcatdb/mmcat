package cz.matfyz.core.utils;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.Nullable;

public class UniqueIdGenerator {

    private static boolean isDeterministic = false;

    /**
     * From now on, all new generators will be deterministic.
     * Use only for tests!
     */
    public static void makeDeterministic() {
        isDeterministic = true;
    }

    /** If this is not null, all generated ids will be a deterministic sequence. */
    private final @Nullable UniqueSequentialGenerator deterministicGenerator;

    private UniqueIdGenerator(@Nullable UniqueSequentialGenerator deterministicGenerator) {
        this.deterministicGenerator = deterministicGenerator;
    }

    public static UniqueIdGenerator create() {
        return new UniqueIdGenerator(isDeterministic ? UniqueSequentialGenerator.create() : null);
    }

    public synchronized String next() {
        if (deterministicGenerator != null)
            return deterministicGenerator.nextString();

        // We use only the first 8 characters to make the ids more readable when debugging.
        // This is *obviously* not a good way of doing this, but it's good enough for now. We can always change it later.
        return UUID.randomUUID().toString().substring(0, 8);
    }

}
