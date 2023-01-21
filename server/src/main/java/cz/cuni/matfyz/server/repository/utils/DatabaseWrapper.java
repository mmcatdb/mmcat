package cz.cuni.matfyz.server.repository.utils;

import cz.cuni.matfyz.server.configuration.DatabaseProperties;
import cz.cuni.matfyz.server.exception.DatabaseErrorException;
import cz.cuni.matfyz.server.exception.PrimaryObjectNotFoundException;
import cz.cuni.matfyz.server.exception.SecondaryObjectNotFoundException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author jachym.bartik
 */
@Component
@Scope("singleton")
public class DatabaseWrapper {

    @Autowired
    private DatabaseProperties databaseProperties;

    private DatabaseWrapper() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseWrapper.class);

    public Connection getConnection() {
        return createConnection();
    }

    private Connection createConnection() {
        try {
            var connectionBuilder = new StringBuilder();
            var connectionString = connectionBuilder
                .append("jdbc:postgresql://")
                .append(databaseProperties.host())
                .append(":")
                .append(databaseProperties.port())
                .append("/")
                .append(databaseProperties.database())
                .append("?user=")
                .append(databaseProperties.username())
                .append("&password=")
                .append(databaseProperties.password())
                .toString();

            return DriverManager.getConnection(connectionString);
        }
        catch (SQLException exception) {
            LOGGER.error("Cannot create connection to the server database.", exception);
            throw new DatabaseErrorException("Cannot create connection to the server database.");
        }
    }

    public <T> T get(DatabaseGetSingleFunction<T> function, String format, Object... arguments) {
        return resolveDatabaseFunction(connection -> {
            SingleOutput<T> output = new SingleOutput<>();
            function.execute(connection, output);

            if (output.isEmpty())
                throw new PrimaryObjectNotFoundException(format, arguments);

            return output.get();
        });
    }

    public <T> T get(DatabaseGetSingleFunction<T> function) {
        return get(function, "");
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
        Connection connection = null;

        try {
            connection = createConnection();
            
            return function.execute(connection);
        }
        catch (SQLException exception) {
            LOGGER.error("Cannot execute SQL query on the server database.", exception);
            throw new DatabaseErrorException("Cannot execute SQL query on the server database.");
        }
        catch (JsonProcessingException exception) {
            LOGGER.error("Cannot parse from or to JSON.", exception);
            throw new DatabaseErrorException("Cannot parse from or to JSON.");
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException exception) {
                    LOGGER.error("Cannot close connection to the server database.", exception);
                    throw new DatabaseErrorException("Cannot close connection to the server database.");
                }
            }
        }
    }

    public static <T, I> T join(Function<I, T> joinFunction, I input) {
        try {
            return joinFunction.apply(input);
        }
        catch (PrimaryObjectNotFoundException exception) {
            throw new SecondaryObjectNotFoundException(exception.getMessage());
        }
    }

    public static <T, I, F> List<T> joinMultiple(RepositoryPredicateFunction<I, F> predicateFunction, RepositoryTransformFunction<T, I, F> transformFunction, List<I> inputs, List<F> objects, Function<I, String> errorMessage) {
        return inputs.stream().map((I input) -> {
            final var result = objects.stream().filter((F object) -> predicateFunction.execute(input, object)).findFirst();
            if (!result.isPresent())
                throw new SecondaryObjectNotFoundException(errorMessage.apply(input));

            return transformFunction.execute(input, result.get());
        }).toList();
    }

}