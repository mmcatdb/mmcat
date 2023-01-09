package cz.cuni.matfyz.transformations.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.wrappermongodb.MongoDBDDLWrapper;
import cz.cuni.matfyz.wrappermongodb.MongoDBDatabaseProvider;
import cz.cuni.matfyz.wrappermongodb.MongoDBPullWrapper;
import cz.cuni.matfyz.wrappermongodb.MongoDBPushWrapper;

import java.nio.file.Paths;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class MongoDBToMongoDBTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBToMongoDBTests.class);

    private static final MongoDBDatabaseProvider mongodbProvider = DatabaseSetup.createMongoDBDatabaseProvider();

    @BeforeAll
    public static void setupMongoDB() {
        try {
            var url = ClassLoader.getSystemResource("setupMongodb.js");
            String pathToFile = Paths.get(url.toURI()).toAbsolutePath().toString();
            DatabaseSetup.executeMongoDBScript(pathToFile);
        }
        catch (Exception exception) {
            LOGGER.error("MongoDB setup error: ", exception);
        }
    }

    private static MongoDBPullWrapper createPullWrapper() {
        var wrapper = new MongoDBPullWrapper();
        wrapper.injectDatabaseProvider(mongodbProvider);

        return wrapper;
    }

    private PullToDDLAndDMLTestBase testBase;

    @BeforeEach
    public void setupTestBase() {
        testBase = new PullToDDLAndDMLTestBase(createPullWrapper(), new MongoDBDDLWrapper(), new MongoDBPushWrapper());
    }

    @Test
    public void basicTest() {
        var data = new TestData();
        var schema = data.createDefaultSchemaCategory();
        var order = schema.getObject(data.orderKey);
        
        testBase.setAll(
            "TODO",
            schema,
            order,
            "basic",
            data.path_orderRoot()
        );

        testBase.testAlgorithm();
    }

    @Test
    public void test() throws Exception {
        var data = new TestData();
        ComplexProperty path = data.path_orderRoot();
        LOGGER.trace(path.toString());
        var json = path.toJSON();
        LOGGER.trace(json.toString());

        var parsedPath = new AccessPath.Builder().fromJSON(new JSONObject(json.toString()));
        LOGGER.trace(parsedPath.toString());

        assertEquals(path.toString(), parsedPath.toString());
    }

    @Test
    public void complex_arrayTest() {
        var data = new TestData();
        var schema = data.createDefaultSchemaCategory();
        var order = schema.getObject(data.orderKey);
        
        testBase.setAll(
            "TODO",
            schema,
            order,
            "complex_array",
            data.path_items()
        );

        testBase.testAlgorithm();
    }
}
