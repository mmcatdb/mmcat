package cz.cuni.matfyz.server.exception;

/**
 * @author jachymb.bartik
 */
public abstract class RepositoryException extends RuntimeException {
    
    protected RepositoryException(String errorMessage) {
        super(errorMessage);
    }

}
