package cz.cuni.matfyz.server.exception;

/**
 * @author jachymb.bartik
 */
public class DatabaseErrorException extends RepositoryException {
    
    public DatabaseErrorException(String errorMessage) {
        super(errorMessage);
    }

    public DatabaseErrorException(String format, Object... arguments) {
        super(String.format(format, arguments));
    }

}
