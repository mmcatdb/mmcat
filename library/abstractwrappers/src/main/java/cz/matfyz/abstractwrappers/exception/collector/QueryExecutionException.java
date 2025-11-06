package cz.matfyz.abstractwrappers.exception.collector;

/**
 * Exception thrown from AbsatractConnection when some error during evaluation of a query occur
 * @deprecated Use some specialization of NamedException instead.
 */
public class QueryExecutionException extends WrapperException {
    public QueryExecutionException(String message) { super(message); }
    public QueryExecutionException(Throwable cause) { super(cause); }
    public QueryExecutionException(String message, Throwable cause) { super(message, cause); }
}
