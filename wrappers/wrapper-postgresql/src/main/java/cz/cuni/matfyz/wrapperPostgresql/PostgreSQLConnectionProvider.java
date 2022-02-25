package cz.cuni.matfyz.wrapperPostgresql;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author jachymb.bartik
 */
public class PostgreSQLConnectionProvider implements ConnectionProvider
{
    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    private Connection connection;

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
        if (connection == null)
        {
            try
            {
                buildConnection();
            }
            catch (SQLException exception)
            {
                System.out.println(exception);
            }
        }

        return connection;
    }

    public void buildConnection() throws SQLException
    {
        this.connection = createConnectionFromCredentials(host, port, database, username, password);
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
        
        System.out.println("Executing: " + beforePasswordString + "********" + afterPasswordString);

        String commandString = beforePasswordString + password + afterPasswordString;
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(commandString);
        process.waitFor();

        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = bufferReader.readLine()) != null)
            System.out.println(line);
    }
}