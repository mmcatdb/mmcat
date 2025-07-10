package cz.matfyz.core.exception;

/**
 * A wrapper class for all exceptions other than ours (i.e., other than those that extend the {@link NamedException}).
 */
public class OtherException extends NamedException {

    public OtherException(Exception exception) {
        super("other", exception.getMessage(), exception);
    }

}
