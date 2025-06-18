package cz.cuni.matfyz.collector.wrappers.neo4j.components;

import cz.matfyz.abstractwrappers.collector.components.AbstractConnection;
import cz.matfyz.abstractwrappers.exception.collector.QueryExecutionException;

import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;
import cz.cuni.matfyz.collector.wrappers.neo4j.Neo4jResources;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.summary.ResultSummary;

/**
 * Class which is responsible to connect to neo4j and enable query execution
 */
public class Neo4jConnection extends AbstractConnection<Result, String, ResultSummary> {
    private final Session _querySession;
    private final Session _planSession;
    private final Driver _neo4jDriver;

    public Neo4jConnection(Driver neo4jDriver, String datasetName, WrapperExceptionsFactory exceptionsFactory) {
        super(exceptionsFactory);
        _querySession = neo4jDriver.session(
                SessionConfig.builder().withDatabase(datasetName).withDefaultAccessMode(AccessMode.READ).build()
        );
        _planSession = neo4jDriver.session(
                SessionConfig.builder().withDatabase(datasetName).withDefaultAccessMode(AccessMode.READ).build()
        );
        _neo4jDriver = neo4jDriver;
    }

    /**
     * Method which is responsible for executing query and caching and parsing result to CachedResult
     * @param query inputted query
     * @return instance of CachedResult
     * @throws QueryExecutionException when some Neo4jException or ParseException occur during process
     */
    @Override
    public Result executeQuery(String query) throws QueryExecutionException {
        try {
            return _querySession.run(query);
        } catch (Neo4jException e) {
            throw getExceptionsFactory().queryExecutionFailed(e);
        }
    }

    // Result must be iterated before consume, otherwise exception is thrown
    private ResultSummary _iterateResultPlanAndConsume(Result planResult) {
        while (planResult.hasNext()) {
            var record = planResult.next();
            System.out.println(record);
        }
        return planResult.consume();
    }

    @Override
    public ResultWithPlan<Result, ResultSummary> executeWithExplain(String query) throws QueryExecutionException {
        try {
            ResultSummary plan = _iterateResultPlanAndConsume(_planSession.run(Neo4jResources.getExplainPlanQuery(query)));
            Result result = _querySession.run(query);
            return new ResultWithPlan<>(result, plan);
        } catch (Neo4jException e) {
            throw getExceptionsFactory().queryExecutionWithExplainFailed(e);
        }
    }

    @Override
    public boolean isOpen() {
        return _querySession.isOpen();
    }

    /**
     * Method which implements interface AutoClosable and closes all resources after query evaluation is ended
     */
    @Override
    public void close() {
        _planSession.close();
        _querySession.close();
    }
}
