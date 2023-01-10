package cz.cuni.matfyz.server.exception;

/**
 * @author jachymb.bartik
 */
public class SecondaryObjectNotFoundException extends RepositoryException {
    
    public SecondaryObjectNotFoundException(String errorMessage) {
        super(errorMessage);
    }

    public SecondaryObjectNotFoundException(String format, Object... arguments) {
        super(String.format(format, arguments));
    }

}
