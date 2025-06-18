package cz.matfyz.abstractwrappers.collector.components;

import cz.matfyz.core.collector.DataModel;
import cz.matfyz.abstractwrappers.exception.collector.ConnectionException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;

public class ExecutionContext<TResult, TQuery, TPlan> {
    private final WrapperExceptionsFactory _exceptionsFactory;
    private final DataModel _model;
    private final String _query;

    public ExecutionContext(String query, WrapperExceptionsFactory exceptionsFactory, DataModel model) {
        _query = query;
        _exceptionsFactory = exceptionsFactory;
        _model = model;
    }

    private AbstractConnection<TResult, TQuery, TPlan> _connection = null;


    public void setConnection(AbstractConnection<TResult, TQuery, TPlan> connection) {
        if (_connection == null)
            _connection = connection;
    }

    public <T> T getConnection(Class<T> clazz) throws ConnectionException {
        if (_connection == null)
            throw _exceptionsFactory.connectionIsNull();
        else if (_connection.isOpen())
            return clazz.cast(_connection);
        else
            throw _exceptionsFactory.connectionNotOpen();
    }

    public AbstractConnection<TResult, TQuery, TPlan> getConnection() throws ConnectionException {
        if (_connection == null)
            throw _exceptionsFactory.connectionIsNull();
        else if (_connection.isOpen())
            return _connection;
        else
            throw _exceptionsFactory.connectionNotOpen();
    }

    public <TFactory> TFactory getExceptionsFactory(Class<TFactory> factoryClass) {
        return factoryClass.cast(_exceptionsFactory);
    }

    public WrapperExceptionsFactory getExceptionsFactory() {
        return _exceptionsFactory;
    }

    public String getQuery() {
        return _query;
    }

    public DataModel getModel() {
        return _model;
    }
}
