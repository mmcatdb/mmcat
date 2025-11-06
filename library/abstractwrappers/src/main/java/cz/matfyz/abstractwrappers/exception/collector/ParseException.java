package cz.matfyz.abstractwrappers.exception.collector;

/**
 * Exception thrown from instance of AbstractParser when some error occur during parsing of explain tree or result
 * @deprecated Use some specialization of NamedException instead.
 */
public class ParseException extends WrapperException {
    public ParseException(String message) { super(message); }
    public ParseException(Throwable cause) { super(cause); }
    public ParseException(String message, Throwable cause) { super(message, cause); }
}
