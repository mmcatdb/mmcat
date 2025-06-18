package cz.matfyz.abstractwrappers.collector.components;

import cz.matfyz.abstractwrappers.exception.collector.QueryExecutionException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;

/**
 * Class which encapsulates connection resources to some database and provides unified API for communication with this database
 * @param <TPlan> type of execution plan which will be parsed by parser
 * @param <TResult> type of result which will be parsed by parser
 * @param <TQuery> type of query on which all execute methods will be called
 */
public abstract class AbstractConnection<TResult, TQuery, TPlan> extends AbstractComponent implements AutoCloseable {

    public AbstractConnection(WrapperExceptionsFactory exceptionsFactory) {
        super(exceptionsFactory);
    }

    /**
     * Method for executing query from execution. Whole result from database is then cached into memory
     * @param query inputted query
     * @return result of this query
     * @throws QueryExecutionException when some problem occur during the process
     */
    public abstract TResult executeQuery(TQuery query) throws QueryExecutionException;

    public abstract ResultWithPlan<TResult, TPlan> executeWithExplain(TQuery query) throws QueryExecutionException;

    public abstract boolean isOpen();

    @Override
    public void close() throws QueryExecutionException {}

    public record ResultWithPlan<TResult, TPlan>(TResult result, TPlan plan) {}
}
