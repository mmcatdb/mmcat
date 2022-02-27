package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.abstractwrappers.PullWrapperOptions;
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
public class MongoDBTests
{
    private static final MongoDBDatabaseProvider databaseProvider = new MongoDBDatabaseProvider(
        Config.get("mongodb.host"),
        Config.get("mongodb.port"),
        Config.get("mongodb.database"),
        Config.get("mongodb.username"),
        Config.get("mongodb.password")
    );

    @BeforeAll
    public static void setupDB()
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
    public void createDBProvider_DoesNotThrow()
    {
        assertDoesNotThrow(() -> createPullWrapper());
    }

    @Test
    public void readFromDB_DoesNotThrow()
    {
        assertDoesNotThrow(() -> {
            var inputWrapper = createPullWrapper();
            var dbContent = inputWrapper.readCollectionAsStringForTests("database.getCollection(\"basic_order\");");
            System.out.println(dbContent);
        });
    }

    private void pullForestTestAlgorithm(String collectionName, String dataFileName, ComplexProperty accessPath) throws Exception
    {
        var inputWrapper = createPullWrapper();

        var forest = inputWrapper.pullForest(accessPath, new PullWrapperOptions.Builder().buildWithKindName(collectionName));
        System.out.println(forest);

        var dummyWrapper = new DummyPullWrapper();
        var url = ClassLoader.getSystemResource("modelToCategory/" + dataFileName);
        String fileName = Paths.get(url.toURI()).toAbsolutePath().toString();

        var expectedForest = dummyWrapper.pullForest(accessPath, new PullWrapperOptions.Builder().buildWithKindName(fileName));
        
        assertEquals(expectedForest.toString(), forest.toString());
    }

    @Test
    public void getForestForBasicTest() throws Exception
    {
        pullForestTestAlgorithm("basic", "1BasicTest.json", new TestData().path_order());
    }

    @Test
    public void getForestForStructureTest() throws Exception
    {
        pullForestTestAlgorithm("structure", "2StructureTest.json", new TestData().path_nestedDoc());
    }

    @Test
    public void getForestForSimpleArrayTest() throws Exception
    {
        pullForestTestAlgorithm("simple_array", "3SimpleArrayTest.json", new TestData().path_array());
    }

    @Test
    public void getForestForComplexArrayTest() throws Exception
    {
        pullForestTestAlgorithm("complex_array", "4ComplexArrayTest.json", new TestData().path_items());
    }

    @Test
    public void getForestForEmptyArrayTest() throws Exception
    {
        pullForestTestAlgorithm("empty_array", "9EmptyArrayTest.json", new TestData().path_items());
    }

    @Test
    public void getForestForComplexMapTest() throws Exception
    {
        pullForestTestAlgorithm("complex_map", "10ComplexMapTest.json", new TestData().path_address());
    }
}
