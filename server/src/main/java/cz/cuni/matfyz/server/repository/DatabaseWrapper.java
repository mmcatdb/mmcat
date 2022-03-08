package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Function;


/**
 * 
 * @author jachym.bartik
 */
public abstract class DatabaseWrapper
{
    // TODO pro každou úlohu dělat nové connection a pak jej zavřít

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
            // TODO soubor s databází - zjistit, pokud existuje, a když ne, tak vytvořit
                // konfigurační možnost databáze
                // kdyžtak do .gitignore

            String configuredDbPath = Config.get("databaseFile.path");
            String databasePath = configuredDbPath != null ? configuredDbPath : "dbPathNotSet.db";
            String connectionUrl = "jdbc:sqlite:" + databasePath;

            return DriverManager.getConnection(connectionUrl);
        }
        catch (SQLException exception)
        {
            System.out.println(exception.getMessage());
        }

        return null;
    }

    public static void executeDbFunction(Function<Connection, Void> function)
    {
        Connection connection = null;
        try
        {
            connection = createConnection();
            function.apply(connection); // TODO
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException exception) // TODO
                {
                    System.out.println(exception);
                }
            }
        }
    }
}
