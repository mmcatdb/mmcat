package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.exception.ExecuteException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLControlWrapper implements AbstractControlWrapper {
    
    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLControlWrapper.class);

    static final String TYPE = "postgresql";

    private PostgreSQLProvider provider;

    public PostgreSQLControlWrapper(PostgreSQLProvider provider) {
        this.provider = provider;
    }

    @Override public void execute(Collection<AbstractStatement> statements) {
        try (
            Connection connection = provider.getConnection();
        ) {
            // TODO transactions?
            for (final var statement : statements) {
                try (
                    PreparedStatement preparedStatement = connection.prepareStatement(statement.getContent());
                ) {
                    LOGGER.info("Execute PostgreSQL statement:\n{}", preparedStatement);
                    preparedStatement.execute();
                }
            }
        }
        catch (Exception e) {
            throw new ExecuteException(e, statements);
        }
    }

    @Override public void execute(Path path) {
        try {
            String script = Files.readString(path);
            // Split the queries by the ; character, followed by any number of whitespaces and newline.
            final var statements = Stream.of(script.split(";\\s*\n"))
                .map(String::strip)
                .filter(s -> !s.isBlank())
                .map(s -> (AbstractStatement) new PostgreSQLStatement(s))
                .toList();

            execute(statements);
        }
        catch (IOException e) {
            throw new ExecuteException(e, path);
        }
    }

    // String beforePasswordString = new StringBuilder()
    //     .append("psql postgresql://")
    //     .append(PostgreSQL.USERNAME)
    //     .append(":")
    //     .toString();

    // String afterPasswordString = new StringBuilder()
    //     .append("@")
    //     .append(PostgreSQL.HOST)
    //     .append(":")
    //     .append(PostgreSQL.PORT)
    //     .append("/")
    //     .append(PostgreSQL.DATABASE)
    //     .append(" -f ")
    //     .append(path.toString())
    //     .toString();
    
    // LOGGER.info("Executing: " + beforePasswordString + "********" + afterPasswordString);

    // String commandString = beforePasswordString + PostgreSQL.PASSWORD + afterPasswordString;
    // Runtime runtime = Runtime.getRuntime();
    // Process process = runtime.exec(commandString);
    // process.waitFor();

    // BufferedReader bufferReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    // String info = bufferReader.lines().collect(Collectors.joining());
    // LOGGER.info(info);


    @Override public PostgreSQLDDLWrapper getDDLWrapper() {
        return new PostgreSQLDDLWrapper();
    }

    @Override public PostgreSQLICWrapper getICWrapper() {
        return new PostgreSQLICWrapper();
    }

    @Override public PostgreSQLDMLWrapper getDMLWrapper() {
        return new PostgreSQLDMLWrapper();
    }

    @Override public PostgreSQLPullWrapper getPullWrapper() {
        return new PostgreSQLPullWrapper(provider);
    }

    @Override public PostgreSQLPathWrapper getPathWrapper() {
        return new PostgreSQLPathWrapper();
    }

    @Override public PostgreSQLQueryWrapper getQueryWrapper() {
        return new PostgreSQLQueryWrapper();
    }

}