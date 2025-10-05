package cz.matfyz.core.exception;

import java.io.Serializable;

public class ObjexNotFoundException extends CoreException {

    private ObjexNotFoundException(String name, Serializable data) {
        super("objexNotFound." + name, data, null);
    }

    private record MessageData(String message) implements Serializable {}

    /** Use only as a last resord! */
    public static ObjexNotFoundException withMessage(String message) {
        return new ObjexNotFoundException("withMessage", new MessageData(message));
    }
}
