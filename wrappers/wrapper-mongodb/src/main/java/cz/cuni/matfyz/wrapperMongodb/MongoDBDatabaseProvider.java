package cz.cuni.matfyz.wrapperMongodb;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 *
 * @author jachymb.bartik
 */
public class MongoDBDatabaseProvider implements DatabaseProvider
{
    private String username;
    private String password;
    private String port;
    private String database;

    private MongoDatabase databaseInstance;

    public MongoDBDatabaseProvider(String username, String password, String port, String database)
    {
        this.username = username;
        this.password = password;
        this.port = port;
        this.database = database;
    }

    public MongoDatabase getDatabase()
    {
        if (databaseInstance == null)
            buildDatabase();

        return databaseInstance;
    }

    public void buildDatabase()
    {
        this.databaseInstance = createDatabaseFromCredentials(username, password, port, database);
    }

    private static MongoDatabase createDatabaseFromCredentials(String username, String password, String port, String database)
    {
        var connectionBuilder = new StringBuilder();
        var connectionString = connectionBuilder
            .append("mongodb://")
            .append(username)
            .append(":")
            .append(password)
            .append("@localhost:")
            .append(port)
            .append("/")
            .append(database).toString();

        var client = MongoClients.create(connectionString);
        return client.getDatabase(database);
    }

    public void executeScript(String pathToFile) throws Exception
    {
        String beforePasswordString = new StringBuilder()
            .append("mongo --username ")
            .append(username)
            .append(" --password ").toString();

        String afterPasswordString = new StringBuilder()
            .append(" localhost:")
            .append(port)
            .append("/")
            .append(database)
            .append(" ")
            .append(pathToFile).toString();

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