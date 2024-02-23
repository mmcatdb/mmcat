package cz.matfyz.transformations.exception;

import cz.matfyz.core.exception.NamedException;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public abstract class TransformationException extends NamedException {

    protected TransformationException(String name, Serializable data, Throwable cause) {
        super("transformation." + name, data, cause);
    }

}
