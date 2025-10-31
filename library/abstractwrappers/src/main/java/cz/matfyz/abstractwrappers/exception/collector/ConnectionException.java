package cz.matfyz.abstractwrappers.exception.collector;

public class ConnectionException extends WrapperException{
    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(Throwable cause) {
        super(cause);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
