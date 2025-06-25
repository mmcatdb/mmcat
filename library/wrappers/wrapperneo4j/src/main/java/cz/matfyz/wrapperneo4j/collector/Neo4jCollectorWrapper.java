package cz.matfyz.wrapperneo4j.collector;

import cz.matfyz.abstractwrappers.collector.AbstractCollectorWrapper.ConnectionData;
import cz.matfyz.abstractwrappers.collector.CollectorWrapper;
import cz.matfyz.abstractwrappers.collector.components.AbstractComponent;
import cz.matfyz.abstractwrappers.collector.components.AbstractExplainPlanParser;
import cz.matfyz.abstractwrappers.collector.components.AbstractQueryResultParser;
import cz.matfyz.abstractwrappers.collector.components.ExecutionContext;
import cz.matfyz.abstractwrappers.exception.collector.ConnectionException;
import cz.matfyz.abstractwrappers.exception.collector.DataCollectException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.wrapperneo4j.collector.components.Neo4jConnection;
import cz.matfyz.wrapperneo4j.collector.components.Neo4jDataCollector;
import cz.matfyz.wrapperneo4j.collector.components.Neo4jExplainPlanParser;
import cz.matfyz.wrapperneo4j.collector.components.Neo4jQueryResultParser;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.summary.ResultSummary;

/**
 * Class which represents wrapper that is connected to Neo4j database and evaluate queries over it
 */
public class Neo4jCollectorWrapper extends AbstractComponent implements CollectorWrapper, AutoCloseable {

    protected final ConnectionData _connectionData;

    protected AbstractQueryResultParser<Result> _resultParser;

    protected AbstractExplainPlanParser<ResultSummary> _explainPlanParser;

    public final DataModel executeQuery(QueryContent query) throws WrapperException {
        String queryString = query.toString();

        var context = createExecutionContext(queryString, DataModel.CreateForQuery(queryString, _connectionData.systemName(), _connectionData.databaseName()));

        try (var connection = createConnection(context)) {
            context.setConnection(connection);

            var inputQuery = parseInputQuery(queryString, context);
            var explainResult = connection.executeWithExplain(inputQuery);

            var mainResult = _resultParser.parseResultAndConsume(explainResult.result());
            _explainPlanParser.parsePlan(explainResult.plan(), context.getModel());


            var dataCollector = createDataCollector(context);
            dataCollector.collectData(mainResult);

            return context.getModel();
        }
    }

    protected ExecutionContext<Result, String, ResultSummary> createExecutionContext(String query, DataModel model) {
        return new ExecutionContext<>(query, getExceptionsFactory(), model);
    }

    private final Driver _driver;
    public Neo4jCollectorWrapper(ConnectionData connectionData) {
        super(new WrapperExceptionsFactory());
        _connectionData = connectionData;
        _resultParser = createResultParser();
        _explainPlanParser = createExplainPlanParser();

        _driver = GraphDatabase.driver(
                Neo4jResources.getConnectionLink(connectionData.host(), connectionData.port(), connectionData.databaseName()),
                AuthTokens.basic(connectionData.user(), connectionData.password())
        );
    }

    protected AbstractQueryResultParser<Result> createResultParser() {
        return new Neo4jQueryResultParser(getExceptionsFactory());
    }

    protected AbstractExplainPlanParser<ResultSummary> createExplainPlanParser() {
        return new Neo4jExplainPlanParser(getExceptionsFactory());
    }

    protected Neo4jConnection createConnection(ExecutionContext<Result, String, ResultSummary> context) throws ConnectionException {
        return new Neo4jConnection(_driver, _connectionData.databaseName(), getExceptionsFactory());
    }

    protected String parseInputQuery(String query, ExecutionContext<Result, String, ResultSummary> context) {
        return query;
    }

    protected Neo4jDataCollector createDataCollector(ExecutionContext<Result, String, ResultSummary> context) throws DataCollectException {
        try {
            return new Neo4jDataCollector(context, _resultParser, _connectionData.databaseName());
        } catch (ConnectionException e) {
            throw getExceptionsFactory().dataCollectorNotInitialized(e);
        }
    }

    public void close() {
        _driver.close();
    }
}
