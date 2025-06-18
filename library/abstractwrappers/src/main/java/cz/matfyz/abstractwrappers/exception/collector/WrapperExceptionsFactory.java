package cz.matfyz.abstractwrappers.exception.collector;

// TODO: rewrite ConnectionData into ControlWrapper ... likely
// import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.collector.AbstractWrapper;

public class WrapperExceptionsFactory {
    private final AbstractWrapper.ConnectionData _connectionData;

    public WrapperExceptionsFactory(AbstractWrapper.ConnectionData connectionData) {
        _connectionData = connectionData;
    }


    //region ConnectionExceptions initialization
    public ConnectionException connectionIsNull() {
        var message = new Message("connection is null").toString();
        return new ConnectionException(message);
    }

    public ConnectionException connectionNotOpen() {
        var message = new Message("connection is not open").toString();
        return new ConnectionException(message);
    }

    public ConnectionException connectionNotInitialized(Throwable cause) {
        var message = new Message("connection can not be initialized", cause).toString();
        return new ConnectionException(message, cause);
    }
    //endregion

    //region QueryExecutionExceptions initialization
    public QueryExecutionException queryExecutionFailed(Throwable cause) {
        var message = new Message("query execution failed", cause).toString();
        return new QueryExecutionException(message, cause);
    }

    public QueryExecutionException queryExecutionWithExplainFailed(Throwable cause) {
        var message = new Message("query execution with explain failed", cause).toString();
        return new QueryExecutionException(message, cause);
    }
    //endregion

    //region DataCollectException initialization
    public DataCollectException dataCollectionFailed(Throwable cause) {
        var message = new Message("collection of data failed", cause).toString();
        return new DataCollectException(message, cause);
    }

    public DataCollectException dataCollectorNotInitialized(Throwable cause) {
        var message = new Message("data collector can not be initialized", cause).toString();
        return new DataCollectException(message);
    }
    //endregion

    //region ParseExceptions initialization
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
    //endregion

    //region WrapperExceptions initialization
    public WrapperException wrapperInitializationFailed(Throwable cause) {
        var message = new Message("wrapper can not be initialized", cause).toString();
        return new WrapperException(message, cause);
    }
    //endregion

    //region WrapperUnsupportedOperationException initialization
    public WrapperUnsupportedOperationException unsupportedOperation(String operation) {
        var message = new Message("operation '" + operation + "' is not supported").toString();
        return new WrapperUnsupportedOperationException(message);
    }
    //endregion

    protected class Message {
        private final StringBuilder _messageBuilder;

        public Message(String content) {
            _messageBuilder = new StringBuilder()
                    .append("Wrapper(system: ")
                    .append(_connectionData.systemName())
                    .append(", database: ")
                    .append(_connectionData.databaseName())
                    .append(") ")
                    .append(content);
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
}
