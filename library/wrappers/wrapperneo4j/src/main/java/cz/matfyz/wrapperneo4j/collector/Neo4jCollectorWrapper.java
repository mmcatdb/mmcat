package cz.matfyz.wrapperneo4j.collector;

import cz.matfyz.abstractwrappers.collector.CollectorWrapper;
import cz.matfyz.abstractwrappers.exception.collector.ConnectionException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.wrapperneo4j.Neo4jProvider;
import cz.matfyz.wrapperneo4j.collector.components.Neo4jConnection;
import cz.matfyz.wrapperneo4j.collector.components.Neo4jDataCollector;
import cz.matfyz.wrapperneo4j.collector.components.Neo4jExplainPlanParser;
import cz.matfyz.wrapperneo4j.collector.components.Neo4jQueryResultParser;

/**
 * Class which represents wrapper that is connected to Neo4j database and evaluate queries over it
 */
public class Neo4jCollectorWrapper implements CollectorWrapper {

    private final Neo4jProvider provider;

    protected Neo4jQueryResultParser resultParser;

    protected Neo4jExplainPlanParser explainPlanParser;

    public Neo4jCollectorWrapper(Neo4jProvider provider) {
        this.provider = provider;
        resultParser = new Neo4jQueryResultParser();
        explainPlanParser = new Neo4jExplainPlanParser();

        provider.getSession();
    }

    public final DataModel executeQuery(QueryContent query) throws WrapperException {
        final String queryString = query.toString();
        final DataModel dataModel = new DataModel("Neo4j", queryString);

        try (final var connection = new Neo4jConnection(provider)) {
            final var inputQuery = queryString;
            final var explainResult = connection.executeWithExplain(inputQuery);

            final var mainResult = resultParser.parseResultAndConsume(explainResult.result());
            explainPlanParser.parsePlan(explainResult.plan(), dataModel);

            final var dataCollector = new Neo4jDataCollector(dataModel, connection, resultParser, provider.settings.database());
            dataCollector.collectData(mainResult);

            return dataModel;
        } catch (ConnectionException e) {
            throw WrapperExceptionsFactory.getExceptionsFactory().dataCollectorNotInitialized(e);
        }
    }
}
