package cz.matfyz.wrapperneo4j.collector.components;

import cz.matfyz.core.collector.ResultWithPlan;
import cz.matfyz.abstractwrappers.exception.collector.QueryExecutionException;

import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;
import cz.matfyz.wrapperneo4j.Neo4jProvider;
import cz.matfyz.wrapperneo4j.collector.Neo4jResources;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.summary.ResultSummary;

/**
 * Class which is responsible to connect to neo4j and enable query execution
 */
public class Neo4jConnection implements AutoCloseable {
    private final Session querySession;
    private final Session planSession;

    public Neo4jConnection(Neo4jProvider provider) {
        querySession = provider.getSession();
        planSession = provider.getSession();
    }

    /**
     * Method which is responsible for executing query and caching and parsing result to CachedResult
     * @param query inputted query
     * @return instance of CachedResult
     * @throws QueryExecutionException when some Neo4jException or ParseException occur during process
     */
    public Result executeQuery(String query) throws QueryExecutionException {
        try {
            return querySession.run(query);
        } catch (Neo4jException e) {
            throw WrapperExceptionsFactory.getExceptionsFactory().queryExecutionFailed(e);
        }
    }

    // Result must be iterated before consume, otherwise exception is thrown
    private ResultSummary iterateResultPlanAndConsume(Result planResult) {
        while (planResult.hasNext()) {
            var record = planResult.next();
            System.out.println(record);
        }
        return planResult.consume();
    }

    public ResultWithPlan<Result, ResultSummary> executeWithExplain(String query) throws QueryExecutionException {
        try {
            ResultSummary plan = iterateResultPlanAndConsume(planSession.run(Neo4jResources.getExplainPlanQuery(query)));
            Result result = querySession.run(query);
            return new ResultWithPlan<>(result, plan);
        } catch (Neo4jException e) {
            throw WrapperExceptionsFactory.getExceptionsFactory().queryExecutionWithExplainFailed(e);
        }
    }

    public boolean isOpen() {
        return querySession.isOpen();
    }

    /**
     * Method which implements interface AutoClosable and closes all resources after query evaluation is ended
     */
    public void close() {
        planSession.close();
        querySession.close();
    }
}
