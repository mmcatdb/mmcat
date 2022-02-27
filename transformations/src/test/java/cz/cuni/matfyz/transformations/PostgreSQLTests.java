package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.abstractwrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.wrapperDummy.DummyPullWrapper;
import cz.cuni.matfyz.wrapperPostgresql.PostgreSQLConnectionProvider;
import cz.cuni.matfyz.wrapperPostgresql.PostgreSQLPullWrapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.nio.file.Paths;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author jachymb.bartik
 */
public class PostgreSQLTests
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLTests.class);

    private static final PostgreSQLConnectionProvider connectionProvider = new PostgreSQLConnectionProvider(
        Config.get("postgresql.host"),
        Config.get("postgresql.port"),
        Config.get("postgresql.database"),
        Config.get("postgresql.username"),
        Config.get("postgresql.password")
    );

    @BeforeAll
    public static void setupDB()
    {
        try
        {
            var url = ClassLoader.getSystemResource("setupPostgresql.sql");
            String pathToFile = Paths.get(url.toURI()).toAbsolutePath().toString();
            connectionProvider.executeScript(pathToFile);
        }
        catch (Exception exception)
        {
            LOGGER.error("PostgreSQL setup error: ", exception);
        }
    }

    private static PostgreSQLPullWrapper createPullWrapper() throws SQLException
    {
        connectionProvider.buildConnection();
        var wrapper = new PostgreSQLPullWrapper();
        wrapper.injectConnectionProvider(connectionProvider);

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
            var dbContent = inputWrapper.readTableAsStringForTests("SELECT * FROM \"order\";");
            LOGGER.debug("DB content:\n" + dbContent);
        });
    }

    private void pullForestTestAlgorithm(String databaseName, String expectedDataFileName, ComplexProperty accessPath) throws Exception
    {
        var inputWrapper = createPullWrapper();

        var forest = inputWrapper.pullForest(accessPath, new PullWrapperOptions.Builder().buildWithKindName(databaseName));
        LOGGER.debug("Pulled forest:\n" + forest);

        var dummyWrapper = new DummyPullWrapper();
        var url = ClassLoader.getSystemResource("postgresql/" + expectedDataFileName);
        String fileName = Paths.get(url.toURI()).toAbsolutePath().toString();

        var expectedForest = dummyWrapper.pullForest(accessPath, new PullWrapperOptions.Builder().buildWithKindName(fileName));
        
        assertEquals(expectedForest.toString(), forest.toString());
    }

    @Test
    public void getForestForBasicTest() throws Exception
    {
        pullForestTestAlgorithm("order_basic", "1BasicTest.json", new TestData().path_order());
    }

    @Test
    public void getForestForStructureTest() throws Exception
    {
        pullForestTestAlgorithm("order_structure", "2StructureTest.json", new TestData().path_nestedDoc());
    }

/*
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
    */
}
