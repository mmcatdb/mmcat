package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractCollectorWrapper;
import cz.matfyz.abstractwrappers.exception.collector.QueryExecutionException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.core.collector.ResultWithPlan;
import cz.matfyz.wrapperneo4j.collector.Neo4jDataCollector;
import cz.matfyz.wrapperneo4j.collector.Neo4jExplainPlanParser;
import cz.matfyz.wrapperneo4j.collector.Neo4jQueryResultParser;
import cz.matfyz.wrapperneo4j.collector.Neo4jResources;

import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.Result;

public class Neo4jCollectorWrapper implements AbstractCollectorWrapper {

    private final Neo4jProvider provider;
    private final String datasourceIdentifier;

    protected Neo4jQueryResultParser resultParser;

    protected Neo4jExplainPlanParser explainPlanParser;

    public Neo4jCollectorWrapper(Neo4jProvider provider, String datasourceIdentifier) {
        this.provider = provider;
        this.datasourceIdentifier = datasourceIdentifier;
        resultParser = new Neo4jQueryResultParser();
        explainPlanParser = new Neo4jExplainPlanParser();

        provider.getSession();
    }

    public final DataModel executeQuery(QueryContent query) throws WrapperException {
        final var inputQuery = query.toString();
        final DataModel dataModel = new DataModel(datasourceIdentifier, inputQuery);

        final var explainResult = executeWithExplain(inputQuery);

        final var mainResult = resultParser.parseResultAndConsume(explainResult.result());
        explainPlanParser.parsePlan(explainResult.plan(), dataModel);

        final var dataCollector = new Neo4jDataCollector(dataModel, provider, resultParser);
        dataCollector.collectData(mainResult);

        return dataModel;
    }

    private ResultWithPlan<Result, ResultSummary> executeWithExplain(String query) throws QueryExecutionException {
        try (
            final var session = provider.getSession();
        ) {
            final var planResult = session.run(Neo4jResources.getExplainPlanQuery(query));
            // Result must be iterated before consume, otherwise exception is thrown.
            while (planResult.hasNext())
                planResult.next();
            final ResultSummary plan = planResult.consume();

            final Result result = session.run(query);

            return new ResultWithPlan<>(result, plan);
        } catch (Neo4jException e) {
            throw WrapperExceptionsFactory.getExceptionsFactory().queryExecutionWithExplainFailed(e);
        }
    }

}
