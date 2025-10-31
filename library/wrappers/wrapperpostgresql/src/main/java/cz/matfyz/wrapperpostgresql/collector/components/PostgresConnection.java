package cz.matfyz.wrapperpostgresql.collector.components;

import cz.matfyz.core.collector.ResultWithPlan;
import cz.matfyz.abstractwrappers.exception.collector.ConnectionException;
import cz.matfyz.abstractwrappers.exception.collector.QueryExecutionException;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;
import cz.matfyz.wrapperpostgresql.collector.PostgreSQLExceptionsFactory;
import cz.matfyz.wrapperpostgresql.collector.PostgreSQLResources;

import java.sql.*;

/**
 * Class representing connection to PostgreSQL database and enables to evaluate queries.
 * @deprecated
 */
public class PostgresConnection implements AutoCloseable {
    private final Connection connection;
    private final Statement statement;
    public PostgresConnection(PostgreSQLProvider provider) throws ConnectionException {
        try {
            connection = provider.getConnection();
            connection.setReadOnly(true);
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw PostgreSQLExceptionsFactory.getExceptionsFactory().connectionNotInitialized(e);
        }
    }

    public ResultWithPlan<ResultSet, String> executeWithExplain(String query) throws QueryExecutionException {
        try {
            ResultSet planResult = statement.executeQuery(PostgreSQLResources.getExplainPlanQuery(query));
            String plan = null;
            if (planResult.next()) {
                plan = planResult.getString("QUERY PLAN");
            }
            ResultSet result = statement.executeQuery(query);
            return new ResultWithPlan<>(result, plan);
        } catch (SQLException e) {
            throw PostgreSQLExceptionsFactory.getExceptionsFactory().queryExecutionWithExplainFailed(e);
        }
    }

    /**
     * Method executing ordinal query and caching all of its result to CachedResult instance
     * @param query inputted query
     * @return instance of CachedResult
     * @throws QueryExecutionException when SQLException or ParseException occur during the process
     */
    public ResultSet executeQuery(String query) throws QueryExecutionException {
        try {
            return statement.executeQuery(query);
        } catch (SQLException e) {
            throw PostgreSQLExceptionsFactory.getExceptionsFactory().queryExecutionFailed(e);
        }
    }

    /**
     * Method which implements the Interface AutoClosable and closes all database resources after query execution is finished
     * @throws QueryExecutionException from statement.close() and connection.close() if some problem occurs
     */
    @Override
    public void close() throws QueryExecutionException {
        try {
            statement.close();
            connection.close();
        }
        catch (SQLException e) {
            throw new QueryExecutionException(e);
        }
    }
}
