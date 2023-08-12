package cz.matfyz.querying.exception;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public class GeneralException extends QueryingException {

    protected GeneralException(String name, Serializable data, Throwable cause) {
        super("general." + name, data, cause);
    }

    public static GeneralException message(String message) {
        return new GeneralException("message", message, null);
    }

}
