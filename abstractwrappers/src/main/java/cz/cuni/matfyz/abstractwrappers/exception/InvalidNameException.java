package cz.cuni.matfyz.abstractwrappers.exception;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public class InvalidNameException extends WrapperException {

    private record InvalidNameData(
        String type,
        String value,
        boolean isNull
    ) implements Serializable {}
    
    protected InvalidNameException(String type, String value) {
        super("invalidName", new InvalidNameData(type, value == null ? "" : value, value == null), null);
    }

    public static InvalidNameException kind(String name) {
        return new InvalidNameException("kind", name);
    }

    public static InvalidNameException property(String name) {
        return new InvalidNameException("property", name);
    }

    public static InvalidNameException node(String name) {
        return new InvalidNameException("node", name);
    }

}
