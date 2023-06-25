package cz.cuni.matfyz.abstractwrappers.exception;

/**
 * @author jachymb.bartik
 */
public class ExecuteException extends WrapperException {

    public ExecuteException(Exception exception) {
        super("execute", exception.getMessage(), exception);
    }

}
