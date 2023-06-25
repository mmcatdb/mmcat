package cz.cuni.matfyz.abstractwrappers.exception;

/**
 * @author jachymb.bartik
 */
public class PullForestException extends WrapperException {

    public PullForestException(Exception exception) {
        super("pullForest", exception.getMessage(), exception);
    }

}
