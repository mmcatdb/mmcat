package cz.matfyz.wrapperpostgresql.collector.components;

import cz.matfyz.abstractwrappers.collector.components.AbstractConnection.ResultWithPlan;
import cz.matfyz.abstractwrappers.exception.collector.ConnectionException;
import cz.matfyz.abstractwrappers.exception.collector.QueryExecutionException;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;
import cz.matfyz.wrapperpostgresql.collector.PostgreSQLExceptionsFactory;
import cz.matfyz.wrapperpostgresql.collector.PostgreSQLResources;

import java.sql.*;

/**
 * Class representing connection to PostgreSQL database and enables to evaluate queries
 */
public class PostgresConnection implements AutoCloseable {
    private final Connection _connection;
    private final Statement _statement;
    public PostgresConnection(PostgreSQLProvider provider) throws ConnectionException {
        try {
            _connection = provider.getConnection();
            _connection.setReadOnly(true);
            _statement = _connection.createStatement();
        } catch (SQLException e) {
            throw PostgreSQLExceptionsFactory.getExceptionsFactory().connectionNotInitialized(e);
        }
    }

    public ResultWithPlan<ResultSet, String> executeWithExplain(String query) throws QueryExecutionException {
        try {
            ResultSet planResult = _statement.executeQuery(PostgreSQLResources.getExplainPlanQuery(query));
            String plan = null;
            if (planResult.next()) {
                plan = planResult.getString("QUERY PLAN");
            }
            ResultSet result = _statement.executeQuery(query);
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
            return _statement.executeQuery(query);
        } catch (SQLException e) {
            throw PostgreSQLExceptionsFactory.getExceptionsFactory().queryExecutionFailed(e);
        }
    }

    /**
     * Method which implements the Interface AutoClosable and closes all database resources after query execution is finished
     * @throws QueryExecutionException from _statement.close() and _connection.close() if some problem occurs
     */
    @Override
    public void close() throws QueryExecutionException {
        try {
            _statement.close();
            _connection.close();
        }
        catch (SQLException e) {
            throw new QueryExecutionException(e);
        }
    }
}
