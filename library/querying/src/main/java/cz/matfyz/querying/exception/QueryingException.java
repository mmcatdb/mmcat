package cz.matfyz.querying.exception;

import cz.matfyz.core.exception.NamedException;

import java.io.Serializable;

public abstract class QueryingException extends NamedException {

    protected QueryingException(String name, Serializable data, Throwable cause) {
        super("querying." + name, data, cause);
    }

}
