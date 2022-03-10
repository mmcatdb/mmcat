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
    public static Connection getConnection()
    {
        return createConnection();
    }

    private static Connection createConnection()
    {
        try
        {
            // TODO soubor s databází - zjistit, pokud existuje, a když ne, tak vytvořit
                // konfigurační možnost databáze
                // kdyžtak do .gitignore
                // platí to stále po změně na postgress?

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
/*
            String configuredDbPath = Config.get("databaseFile.path");
            String databasePath = configuredDbPath != null ? configuredDbPath : "dbPathNotSet.db";
            String connectionUrl = "jdbc:sqlite:" + databasePath;

            return DriverManager.getConnection(connectionUrl);
*/
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
