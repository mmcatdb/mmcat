package cz.cuni.matfyz.evolution.exception;

/**
 * @author jachymb.bartik
 */
public abstract class MappingEvolutionException extends RuntimeException {
    
    protected MappingEvolutionException(String errorMessage) {
        super(errorMessage);
    }

}
