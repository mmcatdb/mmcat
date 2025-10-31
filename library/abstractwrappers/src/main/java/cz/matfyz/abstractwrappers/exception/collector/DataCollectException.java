package cz.matfyz.abstractwrappers.exception.collector;

/**
 * Exception thrown from instance of AbstractDataCollector when some problem occur during collecting statistical data
 */
public class DataCollectException extends WrapperException {
    public DataCollectException(Throwable cause) { super(cause); }
    public DataCollectException(String message) { super(message); }
    public DataCollectException(String message, Throwable cause) { super(message, cause); }
}
