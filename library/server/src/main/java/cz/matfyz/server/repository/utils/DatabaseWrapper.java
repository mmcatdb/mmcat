package cz.matfyz.server.repository.utils;

import cz.matfyz.core.exception.OtherException;
import cz.matfyz.server.configuration.DatabaseProperties;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.exception.NotFoundException;
import cz.matfyz.server.exception.RepositoryException;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider.PostgreSQLSettings;

import java.sql.Connection;
import java.sql.SQLException;
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
                true
            ));

        return connectionProvider;
    }

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

    public <T> List<T> getMultiple(DatabaseGetArrayFunction<T> function) {
        return resolveDatabaseFunction(connection -> {
            ArrayOutput<T> output = new ArrayOutput<>();
            function.execute(connection, output);

            return output.get();
        });
    }

    public boolean getBoolean(DatabaseGetBooleanFunction function) {
        Boolean resolvedOutput = resolveDatabaseFunction(connection -> {
            BooleanOutput output = new BooleanOutput();
            function.execute(connection, output);

            return output.get();
        });

        // This is necessary because the resolveDatabaseFunction returns null in case of any error.
        // Null as a Boolean cannot be casted to boolean so we have to check it manually.
        return resolvedOutput != null && resolvedOutput;
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

    public void execute(String ...statements) {
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

    /*
    public static <T, I, F> List<T> joinMultiple(RepositoryPredicateFunction<I, F> predicateFunction, RepositoryTransformFunction<T, I, F> transformFunction, List<I> inputs, List<F> objects, Function<I, String> errorMessage) {
        return inputs.stream().map((I input) -> {
            final var result = objects.stream().filter((F object) -> predicateFunction.execute(input, object)).findFirst();
            if (!result.isPresent())
                throw NotFoundException.secondaryObject(errorMessage.apply(input));

            return transformFunction.execute(input, result.get());
        }).toList();
    }
    */

}
