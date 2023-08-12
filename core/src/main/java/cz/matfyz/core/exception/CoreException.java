package cz.matfyz.core.exception;

import java.io.Serializable;

/**
 * A base class for all core exceptions.
 * @author jachymb.bartik
 */
public abstract class CoreException extends NamedException {

    protected CoreException(String name, Serializable data, Throwable cause) {
        super("core." + name, data, cause);
    }

}
