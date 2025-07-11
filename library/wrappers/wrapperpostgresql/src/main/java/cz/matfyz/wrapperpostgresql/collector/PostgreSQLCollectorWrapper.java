package cz.matfyz.wrapperpostgresql.collector;

import cz.matfyz.abstractwrappers.collector.CollectorWrapper;
import cz.matfyz.abstractwrappers.exception.collector.*;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;
import cz.matfyz.wrapperpostgresql.PostgreSQLQuery;
import cz.matfyz.wrapperpostgresql.collector.components.PostgresConnection;
import cz.matfyz.wrapperpostgresql.collector.components.PostgresDataCollector;
import cz.matfyz.wrapperpostgresql.collector.components.PostgresExplainPlanParser;
import cz.matfyz.wrapperpostgresql.collector.components.PostgresQueryResultParser;

/**
 * Class which represents the wrapper operating over PostgreSQL database
 */
public class PostgreSQLCollectorWrapper implements CollectorWrapper {

    protected final PostgreSQLProvider provider;
    private final String datasourceIdentifier;

    protected final PostgresQueryResultParser resultParser;

    protected final PostgresExplainPlanParser explainPlanParser;

    public PostgreSQLCollectorWrapper(PostgreSQLProvider provider, String datasourceIdentifier) {
        this.provider = provider;
        this.datasourceIdentifier = datasourceIdentifier;
        resultParser = new PostgresQueryResultParser();
        explainPlanParser = new PostgresExplainPlanParser();
    }

    public final DataModel executeQuery(QueryContent query) throws WrapperException {
        assert query instanceof PostgreSQLQuery;
        final var postgresQuery = (PostgreSQLQuery)query;

        final var dataModel = new DataModel(datasourceIdentifier, postgresQuery.toString());

        try (
            final var connection = new PostgresConnection(provider);
        ) {
            final var explainResult = connection.executeWithExplain(postgresQuery.toString());

            final var mainResult = resultParser.parseResultAndConsume(explainResult.result(), postgresQuery.tableColumns);
            explainPlanParser.parsePlan(explainResult.plan(), dataModel);


            final var dataCollector = new PostgresDataCollector(provider.settings.database(), dataModel, connection, resultParser);
            dataCollector.collectData(mainResult);
            return dataModel;
        } catch (ConnectionException e) {
            throw PostgreSQLExceptionsFactory.getExceptionsFactory().dataCollectorNotInitialized(e);
        }
    }
}
