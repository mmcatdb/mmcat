package cz.matfyz.abstractwrappers.collector;

import cz.matfyz.core.collector.DataModel;
import cz.matfyz.abstractwrappers.collector.components.*;
import cz.matfyz.abstractwrappers.exception.collector.*;

public abstract class AbstractCollectorWrapper<TResult, TQuery, TPlan> extends AbstractComponent implements CollectorWrapper, AutoCloseable {

    protected final ConnectionData _connectionData;

    protected AbstractQueryResultParser<TResult> _resultParser;

    protected AbstractExplainPlanParser<TPlan> _explainPlanParser;

    public AbstractCollectorWrapper(ConnectionData connectionData, WrapperExceptionsFactory exceptionsFactory) {
        super(exceptionsFactory);
        _connectionData = connectionData;
        _resultParser = createResultParser();
        _explainPlanParser = createExplainPlanParser();
    }

    public AbstractCollectorWrapper(ConnectionData connectionData) {
        super(new WrapperExceptionsFactory());
        _connectionData = connectionData;
        _resultParser = createResultParser();
        _explainPlanParser = createExplainPlanParser();
    }

    protected abstract AbstractQueryResultParser<TResult> createResultParser();

    protected abstract AbstractExplainPlanParser<TPlan> createExplainPlanParser();

    public final DataModel executeQuery(String query) throws WrapperException {
        var context = createExecutionContext(query, DataModel.CreateForQuery(query, _connectionData.systemName, _connectionData.databaseName));

        try (var connection = createConnection(context)) {
            context.setConnection(connection);
            setDependenciesBeforeExecutionIfNeeded(context);

            var inputQuery = parseInputQuery(query, context);
            var explainResult = connection.executeWithExplain(inputQuery);

            var mainResult = _resultParser.parseResultAndConsume(explainResult.result());
            _explainPlanParser.parsePlan(explainResult.plan(), context.getModel());


            var dataCollector = createDataCollector(context);
            dataCollector.collectData(mainResult);

            removeDependenciesAfterExecutionIfPossible(context);
            return context.getModel();
        }
    }

    protected ExecutionContext<TResult, TQuery, TPlan> createExecutionContext(String query, DataModel model) {
        return new ExecutionContext<>(query, getExceptionsFactory(), model);
    }

    protected abstract AbstractConnection<TResult, TQuery, TPlan> createConnection(ExecutionContext<TResult, TQuery, TPlan> context) throws ConnectionException;

    protected void setDependenciesBeforeExecutionIfNeeded(ExecutionContext<TResult, TQuery, TPlan> context) throws WrapperException { }

    protected abstract TQuery parseInputQuery(String query, ExecutionContext<TResult, TQuery, TPlan> context) throws ParseException, WrapperUnsupportedOperationException;

    protected abstract AbstractDataCollector<TResult, TQuery, TPlan> createDataCollector(ExecutionContext<TResult, TQuery, TPlan> context) throws DataCollectException;

    protected void removeDependenciesAfterExecutionIfPossible(ExecutionContext<TResult, TQuery, TPlan> context) throws WrapperException { }



    public record ConnectionData(String host, int port, String systemName, String databaseName, String user, String password) { }
}
