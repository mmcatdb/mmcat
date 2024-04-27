package cz.matfyz.core.exception;

import java.io.Serializable;

/**
 * A base class for all core exceptions.
 */
public abstract class CoreException extends NamedException {

    protected CoreException(String name, Serializable data, Throwable cause) {
        super("core." + name, data, cause);
    }

}
