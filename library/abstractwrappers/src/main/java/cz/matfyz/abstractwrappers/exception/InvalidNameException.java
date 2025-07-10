package cz.matfyz.abstractwrappers.exception;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.Nullable;

public class InvalidNameException extends WrapperException {

    private record InvalidNameData(
        String value,
        boolean isNull
    ) implements Serializable {}

    protected InvalidNameException(String name, @Nullable String value) {
        super("invalidName." + name, new InvalidNameData(value == null ? "" : value, value == null), null);
    }

    public static InvalidNameException kind(@Nullable String value) {
        return new InvalidNameException("kind", value);
    }

    public static InvalidNameException property(@Nullable String value) {
        return new InvalidNameException("property", value);
    }

    public static InvalidNameException node(@Nullable String value) {
        return new InvalidNameException("node", value);
    }

}
