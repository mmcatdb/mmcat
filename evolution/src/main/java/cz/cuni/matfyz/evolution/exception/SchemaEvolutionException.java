package cz.cuni.matfyz.evolution.exception;

/**
 * @author jachymb.bartik
 */
public abstract class SchemaEvolutionException extends RuntimeException {
    
    protected SchemaEvolutionException(String errorMessage) {
        super(errorMessage);
    }

}
