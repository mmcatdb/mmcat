package cz.cuni.matfyz.querying.exception;

import cz.cuni.matfyz.core.exception.NamedException;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public abstract class QueryingException extends NamedException {

    protected QueryingException(String name, Serializable data, Throwable cause) {
        super("querying." + name, data, cause);
    }

}
