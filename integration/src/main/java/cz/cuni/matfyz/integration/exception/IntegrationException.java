package cz.cuni.matfyz.integration.exception;

import cz.cuni.matfyz.core.exception.NamedException;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public abstract class IntegrationException extends NamedException {

    protected IntegrationException(String name, Serializable data, Throwable cause) {
        super("integration." + name, data, cause);
    }

}
