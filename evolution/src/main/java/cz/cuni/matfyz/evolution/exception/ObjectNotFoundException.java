package cz.cuni.matfyz.evolution.exception;

/**
 * @author jachymb.bartik
 */
public class ObjectNotFoundException extends SchemaEvolutionException {
    
    public ObjectNotFoundException(String errorMessage) {
        super(errorMessage);
    }

    public ObjectNotFoundException(String format, Object... arguments) {
        super(String.format(format, arguments));
    }

}
