package cz.matfyz.wrapperpostgresql.collector;

import cz.matfyz.abstractwrappers.collector.AbstractWrapper;
import cz.matfyz.abstractwrappers.exception.collector.DataCollectException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;

public class PostgresExceptionsFactory extends WrapperExceptionsFactory {
    public PostgresExceptionsFactory(AbstractWrapper.ConnectionData connectionData) {
        super(connectionData);
    }

    public DataCollectException tableForColumnNotFound(String columnName) {
        var message = new Message("no table for column '" + columnName + "' was found").toString();
        return new DataCollectException(message);
    }
}
