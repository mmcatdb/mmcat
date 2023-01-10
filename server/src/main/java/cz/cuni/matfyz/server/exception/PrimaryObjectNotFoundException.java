package cz.cuni.matfyz.server.exception;

/**
 * @author jachymb.bartik
 */
public class PrimaryObjectNotFoundException extends RepositoryException {
    
    public PrimaryObjectNotFoundException(String errorMessage) {
        super(errorMessage);
    }

    public PrimaryObjectNotFoundException(String format, Object... arguments) {
        super(String.format(format, arguments));
    }

}
