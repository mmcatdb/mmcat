package cz.matfyz.querying.exception;

import cz.matfyz.core.exception.NamedException;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public class InvalidPlanException extends NamedException {

    protected InvalidPlanException(String name, Serializable data, Throwable cause) {
        super("invalidPlan." + name, data, cause);
    }

    public static InvalidPlanException message(String message) {
        return new InvalidPlanException("message", message, null);
    }

}
