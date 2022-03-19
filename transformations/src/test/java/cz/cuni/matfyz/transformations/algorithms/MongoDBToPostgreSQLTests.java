package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.abstractwrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.wrapperMongodb.MongoDBDatabaseProvider;
import cz.cuni.matfyz.wrapperMongodb.MongoDBPullWrapper;
import cz.cuni.matfyz.wrapperPostgresql.PostgreSQLConnectionProvider;
import cz.cuni.matfyz.wrapperPostgresql.PostgreSQLDDLWrapper;
import cz.cuni.matfyz.wrapperPostgresql.PostgreSQLPushWrapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class MongoDBToPostgreSQLTests
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBToPostgreSQLTests.class);

    private static final MongoDBDatabaseProvider mongodbProvider = new MongoDBDatabaseProvider(
        Config.get("mongodb.host"),
        Config.get("mongodb.port"),
        Config.get("mongodb.database"),
        Config.get("mongodb.username"),
        Config.get("mongodb.password")
    );

    private static final PostgreSQLConnectionProvider postgresqlProvider = new PostgreSQLConnectionProvider(
        Config.get("postgresql.host"),
        Config.get("postgresql.port"),
        Config.get("postgresql.database"),
        Config.get("postgresql.username"),
        Config.get("postgresql.password")
    );

    @BeforeAll
    public static void setupMongoDB()
    {
        try
        {
            var url = ClassLoader.getSystemResource("setupMongodb.js");
            String pathToFile = Paths.get(url.toURI()).toAbsolutePath().toString();
            mongodbProvider.executeScript(pathToFile);
        }
        catch (Exception exception)
        {
            LOGGER.error("MongoDB setup error: ", exception);
        }
    }

    @BeforeAll
    public static void setupPostgresql()
    {
        try
        {
            var url = ClassLoader.getSystemResource("setupPostgresql.sql");
            String pathToFile = Paths.get(url.toURI()).toAbsolutePath().toString();
            postgresqlProvider.executeScript(pathToFile);
        }
        catch (Exception exception)
        {
            LOGGER.error("PostgreSQL setup error: ", exception);
        }
    }

    private static MongoDBPullWrapper createPullWrapper()
    {
        mongodbProvider.buildDatabase();
        var wrapper = new MongoDBPullWrapper();
        wrapper.injectDatabaseProvider(mongodbProvider);

        return wrapper;
    }

    private PullToDDLAndDMLTestBase testBase;

    @BeforeEach
    public void setupTestBase()
    {
        testBase = new PullToDDLAndDMLTestBase(createPullWrapper(), new PostgreSQLDDLWrapper(), new PostgreSQLPushWrapper());
    }

    @Test
    public void basicTest()
    {
        var data = new TestData();
        var schema = data.createDefaultSchemaCategory();
        var order = schema.keyToObject(data.getOrderKey());
        
        testBase.setAll(
            "TODO",
            schema,
            "basic",
            order,
            data.path_order()
        );

        testBase.testAlgorithm();
    }

    @Test
    public void test() throws Exception
    {
        var data = new TestData();
        ComplexProperty path = data.path_order();
        LOGGER.info(path.toString());
        var json = path.toJSON();
        LOGGER.info(json.toString());

        var parsedPath = new AccessPath.Builder().fromJSON(new JSONObject(json.toString()));
        LOGGER.info(parsedPath.toString());

        assertEquals(path.toString(), parsedPath.toString());
    }
}
