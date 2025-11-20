package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractCollectorWrapper;
import cz.matfyz.abstractwrappers.exception.collector.WrapperException;
import cz.matfyz.abstractwrappers.exception.collector.QueryExecutionException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.core.collector.ResultWithPlan;

import java.sql.ResultSet;
import java.sql.SQLException;

import cz.matfyz.wrapperpostgresql.collector.PostgreSQLDataCollector;
import cz.matfyz.wrapperpostgresql.collector.PostgreSQLExceptionsFactory;
import cz.matfyz.wrapperpostgresql.collector.PostgreSQLExplainPlanParser;
import cz.matfyz.wrapperpostgresql.collector.PostgreSQLQueryResultParser;
import cz.matfyz.wrapperpostgresql.collector.PostgreSQLResources;

public class PostgreSQLCollectorWrapper implements AbstractCollectorWrapper {

    protected final PostgreSQLProvider provider;
    private final String datasourceIdentifier;

    protected final PostgreSQLQueryResultParser resultParser;

    protected final PostgreSQLExplainPlanParser explainPlanParser;

    public PostgreSQLCollectorWrapper(PostgreSQLProvider provider, String datasourceIdentifier) {
        this.provider = provider;
        this.datasourceIdentifier = datasourceIdentifier;
        resultParser = new PostgreSQLQueryResultParser();
        explainPlanParser = new PostgreSQLExplainPlanParser();
    }

    public final DataModel executeQuery(QueryContent genericQuery) throws WrapperException {
        if (!(genericQuery instanceof final PostgreSQLQuery query))
            throw PostgreSQLExceptionsFactory.getExceptionsFactory().unsupportedOperation("invalid postgres query");

        final var dataModel = new DataModel(datasourceIdentifier, query.toString());

        final var explainResult = executeWithExplain(query.toString());

        final var mainResult = resultParser.parseResultAndConsume(explainResult.result(), query.tableColumns);
        explainPlanParser.parsePlan(explainResult.plan(), dataModel);

        final var dataCollector = new PostgreSQLDataCollector(dataModel, provider, resultParser, provider.settings.database());
        dataCollector.collectData(mainResult);

        return dataModel;
    }

    private ResultWithPlan<ResultSet, String> executeWithExplain(String query) throws QueryExecutionException {
        try (
            var connection = provider.getConnection();
            var statement = connection.createStatement();
        ) {
            ResultSet planResult = statement.executeQuery(PostgreSQLResources.getExplainPlanQuery(query));
            String plan = null;
            if (planResult.next())
                plan = planResult.getString("QUERY PLAN");

            ResultSet result = statement.executeQuery(query);

            return new ResultWithPlan<>(result, plan);
        } catch (SQLException e) {
            throw PostgreSQLExceptionsFactory.getExceptionsFactory().queryExecutionWithExplainFailed(e);
        }
    }

}
