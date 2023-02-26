package cz.cuni.matfyz.evolution.exception;

/**
 * @author jachymb.bartik
 */
public abstract class EvolutionException extends RuntimeException {
    
    protected EvolutionException(String errorMessage) {
        super(errorMessage);
    }

}
