package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.Config;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * 
 * @author jachym.bartik
 */
public abstract class DatabaseWrapper
{
    private static Connection connection;

    public static Connection getConnection()
    {
        if (connection == null)
            connection = createConnection();

        return connection;
    }

    private static Connection createConnection()
    {
        try
        {
            var url = ClassLoader.getSystemResource(Config.get("databaseFile.name"));
            String pathToDatabaseFile = Paths.get(url.toURI()).toAbsolutePath().toString();
            String connectionUrl = "jdbc:sqlite:" + pathToDatabaseFile;

            return DriverManager.getConnection(connectionUrl);
        }
        catch (URISyntaxException exception)
        {
            System.out.println(exception.getMessage());
        }
        catch (SQLException exception)
        {
            System.out.println(exception.getMessage());
        }

        return null;
    }
}
