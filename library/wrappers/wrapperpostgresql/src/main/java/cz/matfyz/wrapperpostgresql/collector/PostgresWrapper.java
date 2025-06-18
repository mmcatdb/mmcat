package cz.matfyz.wrapperpostgresql.collector;

import cz.matfyz.abstractwrappers.collector.AbstractWrapper;
import cz.matfyz.abstractwrappers.collector.components.AbstractExplainPlanParser;
import cz.matfyz.abstractwrappers.collector.components.AbstractQueryResultParser;
import cz.matfyz.abstractwrappers.collector.components.ExecutionContext;
import cz.matfyz.abstractwrappers.exception.collector.*;
import cz.matfyz.wrapperpostgresql.collector.components.PostgresConnection;
import cz.matfyz.wrapperpostgresql.collector.components.PostgresDataCollector;
import cz.matfyz.wrapperpostgresql.collector.components.PostgresExplainPlanParser;
import cz.matfyz.wrapperpostgresql.collector.components.PostgresQueryResultParser;

import java.sql.*;

/**
 * Class which represents the wrapper operating over PostgreSQL database
 */
public class PostgresWrapper extends AbstractWrapper<ResultSet, String, String> {
    public PostgresWrapper(ConnectionData connectionData) {
        super(connectionData, new PostgresExceptionsFactory(connectionData));
    }


    @Override
    protected AbstractQueryResultParser<ResultSet> createResultParser() {
        return new PostgresQueryResultParser(getExceptionsFactory());
    }

    @Override
    protected AbstractExplainPlanParser<String> createExplainPlanParser() {
        return new PostgresExplainPlanParser(getExceptionsFactory());
    }

    @Override
    protected PostgresConnection createConnection(ExecutionContext<ResultSet, String, String> context) throws ConnectionException {
        return new PostgresConnection(
            PostgresResources.getConnectionLink(
                _connectionData.host(),
                _connectionData.port(),
                _connectionData.databaseName(),
                _connectionData.user(),
                _connectionData.password()
            ),
            getExceptionsFactory()
        );
    }

    @Override
    protected String parseInputQuery(String query, ExecutionContext<ResultSet, String, String> context) {
        return query;
    }

    @Override
    protected PostgresDataCollector createDataCollector(ExecutionContext<ResultSet, String, String> context) throws DataCollectException {
        try {
            return new PostgresDataCollector(context, _resultParser, _connectionData.databaseName());
        } catch (ConnectionException e) {
            throw getExceptionsFactory().dataCollectorNotInitialized(e);
        }
    }

    @Override
    public void close() throws Exception {
    }
}
