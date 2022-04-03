package cz.cuni.matfyz.wrapperPostgresql;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class PostgreSQLConnectionProvider implements ConnectionProvider
{
    private static Logger logger = LoggerFactory.getLogger(PostgreSQLConnectionProvider.class);

    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    // This class is also meant to be instantiated only once (see the MongoDB wrapper) but it currently doesn't use any caching itself.
    // However, some connection pooling can be added in the future.
    //private Connection connection;

    public PostgreSQLConnectionProvider(String host, String port, String database, String username, String password)
    {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public Connection getConnection()
    {
        try {
            return createConnectionFromCredentials(host, port, database, username, password);
        }
        catch (SQLException exception) {
            logger.error("Cannot create connection to PostgreSQL.", exception);
        }

        return null;
    }

    private static Connection createConnectionFromCredentials(String host, String port, String database, String username, String password) throws SQLException
    {
        var connectionBuilder = new StringBuilder();
        var connectionString = connectionBuilder
            .append("jdbc:postgresql://")
            .append(host)
            .append(":")
            .append(port)
            .append("/")
            .append(database)
            .append("?user=")
            .append(username)
            .append("&password=")
            .append(password)
            .toString();

        return DriverManager.getConnection(connectionString);
    }

    public void executeScript(String pathToFile) throws Exception
    {
        String beforePasswordString = new StringBuilder()
            .append("psql postgresql://")
            .append(username)
            .append(":")
            .toString();

        String afterPasswordString = new StringBuilder()
            .append("@")
            .append(host)
            .append(":")
            .append(port)
            .append("/")
            .append(database)
            .append(" -f ")
            .append(pathToFile)
            .toString();
        
        logger.info("Executing: " + beforePasswordString + "********" + afterPasswordString);

        String commandString = beforePasswordString + password + afterPasswordString;
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(commandString);
        process.waitFor();

        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String info = bufferReader.lines().collect(Collectors.joining());
        logger.info(info);
    }
}