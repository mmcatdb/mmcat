package cz.matfyz.server.repository.utils;

import cz.matfyz.core.exception.OtherException;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.exception.NotFoundException;
import cz.matfyz.server.exception.RepositoryException;
import cz.matfyz.server.global.Configuration.DatabaseProperties;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider.PostgreSQLSettings;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class DatabaseWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseWrapper.class);

    @Autowired
    private DatabaseProperties properties;

    private DatabaseWrapper() {}

    public Connection getConnection() {
        try {
            return getConnectionProvider().getConnection();
        }
        catch (OtherException e) {
            LOGGER.error("createConnection", e);
            throw RepositoryException.createConnection(e);
        }
    }

    private PostgreSQLProvider connectionProvider;

    private PostgreSQLProvider getConnectionProvider() {
        if (connectionProvider == null)
            connectionProvider = new PostgreSQLProvider(new PostgreSQLSettings(
                properties.host(),
                properties.port(),
                properties.database(),
                properties.username(),
                properties.password(),
                true,
                true,
                true
            ));

        return connectionProvider;
    }

    // No output

    public void run(DatabaseEmptyFunction function) {
        resolveDatabaseFunction(connection -> {
            function.execute(connection);
            return null;
        });
    }

    public interface DatabaseEmptyFunction {
        void execute(Connection connection) throws SQLException, JsonProcessingException;
    }

    // Single output

    public <T> T get(DatabaseGetSingleFunction<T> function, String type, Id id) {
        return resolveDatabaseFunction(connection -> {
            SingleOutput<T> output = new SingleOutput<>();
            function.execute(connection, output);

            if (output.isEmpty())
                throw NotFoundException.primaryObject(type, id);

            return output.get();
        });
    }

    public <T> T get(DatabaseGetSingleFunction<T> function) {
        return get(function, "", null);
    }

    public interface DatabaseGetSingleFunction<T> {
        void execute(Connection connection, SingleOutput<T> output) throws SQLException, JsonProcessingException;
    }

    public class SingleOutput<T> {

        private T output = null;
        private boolean isEmpty = true;

        public void set(T output) {
            this.output = output;
            this.isEmpty = false;
        }

        public boolean isEmpty() {
            return isEmpty;
        }

        T get() {
            return this.output;
        }

    }

    // Multiple output

    public <T> List<T> getMultiple(DatabaseGetMultipleFunction<T> function) {
        return resolveDatabaseFunction(connection -> {
            MultipleOutput<T> output = new MultipleOutput<>();
            function.execute(connection, output);

            return output.get();
        });
    }

    public interface DatabaseGetMultipleFunction<T> {
        void execute(Connection connection, MultipleOutput<T> output) throws SQLException, JsonProcessingException;
    }

    public class MultipleOutput<T> {

        private List<T> output = new ArrayList<>();

        public void add(T outputItem) {
            this.output.add(outputItem);
        }

        List<T> get() {
            return this.output;
        }

    }

    // Implementation

    interface DatabaseFunction<T> {
        T execute(Connection connection) throws SQLException, JsonProcessingException;
    }

    private <T> T resolveDatabaseFunction(DatabaseFunction<T> function) {
        try (
            Connection connection = getConnection();
        ) {
            return function.execute(connection);
        }
        catch (SQLException e) {
            LOGGER.error("resolveDatabaseFunction", e);
            throw RepositoryException.executeSql(e);
        }
        catch (JsonProcessingException e) {
            LOGGER.error("resolveDatabaseFunction", e);
            throw RepositoryException.processJson(e);
        }
    }

    public void execute(String... statements) {
        try (
            Connection connection = getConnection()
        ) {
            connection.setAutoCommit(false);

            for (final String statement : statements)
                connection.prepareStatement(statement).executeUpdate();

            connection.commit();
        }
        catch (SQLException e) {
            LOGGER.error("executeSql", e);
            throw RepositoryException.executeSql(e);
        }
    }

    public static <T, I> T join(Function<I, T> joinFunction, I input) {
        try {
            return joinFunction.apply(input);
        }
        catch (NotFoundException e) {
            throw e.toSecondaryObject();
        }
    }

}
