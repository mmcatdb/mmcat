package cz.matfyz.abstractwrappers.exception;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public class UnsupportedException extends WrapperException {

    private record UnsupportedData(
        String type,
        String method
    ) implements Serializable {}

    private UnsupportedException(String type, String method) {
        super("unsupported", new UnsupportedData(type, method), null);
    }

    public static UnsupportedException addSimpleProperty(String type) {
        return new UnsupportedException(type, "addSimpleProperty");
    }

    public static UnsupportedException addSimpleArrayProperty(String type) {
        return new UnsupportedException(type, "addSimpleArrayProperty");
    }

    public static UnsupportedException addComplexProperty(String type) {
        return new UnsupportedException(type, "addComplexProperty");
    }

    public static UnsupportedException addComplexArrayProperty(String type) {
        return new UnsupportedException(type, "addComplexArrayProperty");
    }

}
