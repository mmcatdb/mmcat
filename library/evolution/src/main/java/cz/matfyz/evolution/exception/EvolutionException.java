package cz.matfyz.evolution.exception;

import cz.matfyz.core.exception.NamedException;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public abstract class EvolutionException extends NamedException {

    protected EvolutionException(String name, Serializable data, Throwable cause) {
        super("evolution." + name, data, cause);
    }

}
