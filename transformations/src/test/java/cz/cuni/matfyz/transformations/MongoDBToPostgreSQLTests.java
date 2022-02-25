package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.wrapperDummy.DummyPullWrapper;
import cz.cuni.matfyz.wrapperMongodb.MongoDBDatabaseProvider;
import cz.cuni.matfyz.wrapperMongodb.MongoDBPullWrapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

/**
 *
 * @author jachymb.bartik
 */
public class MongoDBToPostgreSQLTests
{
    private static final MongoDBDatabaseProvider databaseProvider = new MongoDBDatabaseProvider(
        Config.get("mongodb.host"),
        Config.get("mongodb.port"),
        Config.get("mongodb.database"),
        Config.get("mongodb.username"),
        Config.get("mongodb.password")
    );

    @BeforeAll
    public static void setupMongoDB()
    {
        try
        {
            var url = ClassLoader.getSystemResource("setupMongodb.js");
            String pathToFile = Paths.get(url.toURI()).toAbsolutePath().toString();
            databaseProvider.executeScript(pathToFile);
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }
    }

    private static MongoDBPullWrapper createPullWrapper()
    {
        databaseProvider.buildDatabase();
        var wrapper = new MongoDBPullWrapper();
        wrapper.injectDatabaseProvider(databaseProvider);

        return wrapper;
    }
}
