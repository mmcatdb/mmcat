package cz.matfyz.abstractwrappers.exception.collector;

public class WrapperUnsupportedOperationException extends WrapperException {

    public WrapperUnsupportedOperationException(String message) {
        super(message);
    }

    public WrapperUnsupportedOperationException(Throwable cause) {
        super(cause);
    }

    public WrapperUnsupportedOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
