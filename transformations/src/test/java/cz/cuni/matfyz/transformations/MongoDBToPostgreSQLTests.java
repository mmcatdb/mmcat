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
        Config.get("mongodb.username"),
        Config.get("mongodb.password"),
        Config.get("mongodb.port"),
        Config.get("mongodb.database")
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

    @Test
    public void createMongodbProvider()
    {
        assertDoesNotThrow(() -> createPullWrapper());
    }

    @Test
    public void readFromMongodb()
    {
        assertDoesNotThrow(() -> {
            var inputWrapper = createPullWrapper();
            var dbContent = inputWrapper.readCollectionAsStringForTests("database.getCollection(\"basic_order\");");
            System.out.println(dbContent);
        });
    }

    private void mongoDBPullForestTestAlgorithm(String collectionName, String dataFileName, ComplexProperty accessPath) throws Exception
    {
        var inputWrapper = createPullWrapper();

        var forest = inputWrapper.pullForest("database.getCollection(\"" + collectionName + "\");", accessPath);
        System.out.println(forest);

        var dummyWrapper = new DummyPullWrapper();
        var url = ClassLoader.getSystemResource("modelToCategory/" + dataFileName);
        String fileName = Paths.get(url.toURI()).toAbsolutePath().toString();

        var expectedForest = dummyWrapper.pullForest(fileName, accessPath);
        
        assertEquals(expectedForest.toString(), forest.toString());
    }

    @Test
    public void getForestForBasicTest() throws Exception
    {
        mongoDBPullForestTestAlgorithm("basic", "1BasicTest.json", new TestData().path_order());
    }

    @Test
    public void getForestForStructureTest() throws Exception
    {
        mongoDBPullForestTestAlgorithm("structure", "2StructureTest.json", new TestData().path_nestedDoc());
    }

    @Test
    public void getForestForSimpleArrayTest() throws Exception
    {
        mongoDBPullForestTestAlgorithm("simple_array", "3SimpleArrayTest.json", new TestData().path_array());
    }

    @Test
    public void getForestForComplexArrayTest() throws Exception
    {
        mongoDBPullForestTestAlgorithm("complex_array", "4ComplexArrayTest.json", new TestData().path_items());
    }

    @Test
    public void getForestForEmptyArrayTest() throws Exception
    {
        mongoDBPullForestTestAlgorithm("empty_array", "9EmptyArrayTest.json", new TestData().path_items());
    }

    @Test
    public void getForestForComplexMapTest() throws Exception
    {
        mongoDBPullForestTestAlgorithm("complex_map", "10ComplexMapTest.json", new TestData().path_address());
    }
}
