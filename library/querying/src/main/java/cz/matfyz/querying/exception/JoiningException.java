package cz.matfyz.querying.exception;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public class JoiningException extends QueryingException {

    protected JoiningException(String name, Serializable data, Throwable cause) {
        super("joining." + name, data, cause);
    }

    public static JoiningException noKinds() {
        return new JoiningException("noKinds", null, null);
    }

    public static JoiningException impossible() {
        return new JoiningException("impossible", null, null);
    }

}
