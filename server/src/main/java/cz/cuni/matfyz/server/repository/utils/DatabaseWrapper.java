package cz.cuni.matfyz.server.repository.utils;

import cz.cuni.matfyz.server.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jachym.bartik
 */
public abstract class DatabaseWrapper {

    private static Logger LOGGER = LoggerFactory.getLogger(DatabaseWrapper.class);

    public static Connection getConnection() {
        return createConnection();
    }

    static Connection createConnection() {
        try {
            var connectionBuilder = new StringBuilder();
            var connectionString = connectionBuilder
                .append("jdbc:postgresql://")
                .append(Config.get("postgresql.host"))
                .append(":")
                .append(Config.get("postgresql.port"))
                .append("/")
                .append(Config.get("postgresql.database"))
                .append("?user=")
                .append(Config.get("postgresql.username"))
                .append("&password=")
                .append(Config.get("postgresql.password"))
                .toString();
    
            return DriverManager.getConnection(connectionString);
        }
        catch (SQLException exception) {
            LOGGER.error("Cannot create connection to the server database.", exception);
        }

        return null;
    }

    public static <OutputType> OutputType get(DatabaseGetSingleFunction<OutputType> function) {
        return resolveDatabaseFunction(connection -> {
            SingleOutput<OutputType> output = new SingleOutput<>();
            function.execute(connection, output);

            return output.get();
        });
    }

    public static <OutputType> List<OutputType> getMultiple(DatabaseGetArrayFunction<OutputType> function) {
        return resolveDatabaseFunction(connection -> {
            ArrayOutput<OutputType> output = new ArrayOutput<>();
            function.execute(connection, output);

            return output.get();
        });
    }

    public static boolean getBoolean(DatabaseGetBooleanFunction function) {
        return resolveDatabaseFunction(connection -> {
            BooleanOutput output = new BooleanOutput();
            function.execute(connection, output);

            return output.get();
        });
    }

    private static <ReturnType> ReturnType resolveDatabaseFunction(DatabaseFunction<ReturnType> function) {
        Connection connection = null;

        try {
            connection = createConnection();
            
            return function.execute(connection);
        }
        catch (SQLException exception) {
            LOGGER.error("Cannot execute SQL query on the server database.", exception);
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (SQLException exception) {
                    LOGGER.error("Cannot close connection to the server database.", exception);
                }
            }
        }

        return null;
    }
    
}