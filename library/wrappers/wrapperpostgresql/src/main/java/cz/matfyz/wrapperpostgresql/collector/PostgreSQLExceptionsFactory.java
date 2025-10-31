package cz.matfyz.wrapperpostgresql.collector;

import cz.matfyz.abstractwrappers.exception.collector.DataCollectException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;

/** @deprecated */
public class PostgreSQLExceptionsFactory extends WrapperExceptionsFactory {
    public DataCollectException tableForColumnNotFound(String columnName) {
        var message = new Message("no table for column '" + columnName + "' was found").toString();
        return new DataCollectException(message);
    }

    static PostgreSQLExceptionsFactory singleton = new PostgreSQLExceptionsFactory();
    public static PostgreSQLExceptionsFactory getExceptionsFactory() { return singleton; }
}
