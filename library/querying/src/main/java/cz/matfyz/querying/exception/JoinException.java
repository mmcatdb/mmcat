package cz.matfyz.querying.exception;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public class JoinException extends QueryingException {

    protected JoinException(String name, Serializable data, Throwable cause) {
        super("join." + name, data, cause);
    }

    public static JoinException impossible() {
        return new JoinException("impossible", null, null);
    }

}
