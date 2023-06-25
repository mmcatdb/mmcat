package cz.cuni.matfyz.server.repository.utils;

import cz.cuni.matfyz.server.configuration.DatabaseProperties;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.exception.NotFoundException;
import cz.cuni.matfyz.server.exception.RepositoryException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
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
        catch (SQLException e) {
            throw RepositoryException.createConnection(e);
        }
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
            final Connection connection = createConnection();
        ) {
            return function.execute(connection);
        }
        catch (SQLException e) {
            throw RepositoryException.executeSql(e);
        }
        catch (JsonProcessingException e) {
            throw RepositoryException.processJson(e);
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
