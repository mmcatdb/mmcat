package cz.matfyz.abstractwrappers.exception.collector;

/** @deprecated */
public class WrapperExceptionsFactory {

    // #region QueryExecution
    public QueryExecutionException queryExecutionWithExplainFailed(Throwable cause) {
        var message = new Message("query execution with explain failed", cause).toString();
        return new QueryExecutionException(message, cause);
    }
    // #endregion

    // #region DataCollection
    public DataCollectException dataCollectionFailed(Throwable cause) {
        var message = new Message("collection of data failed", cause).toString();
        return new DataCollectException(message, cause);
    }

    // #endregion

    // #region ParseExceptions
    public ParseException parseInputQueryFailed(String query, Throwable cause) {
        var message = new Message("parsing of query '" + query + "' failed", cause).toString();
        return new ParseException(message, cause);
    }

    public ParseException parseExplainPlanFailed(Throwable cause) {
        var message = new Message("parsing of explain plan failed", cause).toString();
        return new ParseException(message, cause);
    }

    public ParseException cacheResultFailed(Throwable cause) {
        var message = new Message("caching of query result failed", cause).toString();
        return new ParseException(message, cause);
    }

    public ParseException consumeResultFailed(Throwable cause) {
        var message = new Message("consuming of query result failed", cause).toString();
        return new ParseException(message, cause);
    }
    // #endregion

    // #region Wrapper
    public WrapperException wrapperInitializationFailed(Throwable cause) {
        var message = new Message("wrapper can not be initialized", cause).toString();
        return new WrapperException(message, cause);
    }
    // #endregion

    // #region Unsupported
    public WrapperUnsupportedOperationException unsupportedOperation(String operation) {
        var message = new Message("operation '" + operation + "' is not supported").toString();
        return new WrapperUnsupportedOperationException(message);
    }
    // #endregion

    protected class Message {
        private final StringBuilder _messageBuilder;

        public Message(String content) {
            _messageBuilder = new StringBuilder().append(content);
        }

        public Message(String content, Throwable cause) {
            this(content);
            if (cause != null)
                _messageBuilder.append(" because of: ").append(cause.getMessage());
        }

        @Override
        public String toString() {
            return _messageBuilder.append(".").toString();
        }
    }

    public static WrapperExceptionsFactory getExceptionsFactory() { return new WrapperExceptionsFactory(); }
}
