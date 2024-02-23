package cz.matfyz.abstractwrappers.exception;

import cz.matfyz.core.exception.NamedException;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public abstract class WrapperException extends NamedException {

    protected WrapperException(String name, Serializable data, Throwable cause) {
        super("wrapper." + name, data, cause);
    }

}
