package cz.cuni.matfyz.transformations.algorithms;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cz.cuni.matfyz.abstractwrappers.AbstractStatement;
import cz.cuni.matfyz.abstractwrappers.PullWrapperOptions;
import cz.cuni.matfyz.abstractwrappers.exception.ExecuteException;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.tests.TestData;
import cz.cuni.matfyz.wrapperdummy.DummyPullWrapper;
import cz.cuni.matfyz.wrapperneo4j.Neo4jControlWrapper;
import cz.cuni.matfyz.wrapperneo4j.Neo4jPullWrapper;
import cz.cuni.matfyz.wrapperneo4j.Neo4jSessionProvider;
import cz.cuni.matfyz.wrapperneo4j.Neo4jStatement;

import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author jachymb.bartik
 */
public class Neo4jTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jTests.class);

    private static final Neo4jSessionProvider sessionProvider = DatabaseSetup.createNeo4jSessionProvider();

    @BeforeAll
    public static void setupDB() {
        try {
            var url = ClassLoader.getSystemResource("setupNeo4j.cypher");
            String pathToFile = Paths.get(url.toURI()).toAbsolutePath().toString();
            DatabaseSetup.executeNeo4jScript(pathToFile);
        }
        catch (Exception exception) {
            LOGGER.error("Neo4j setup error: ", exception);
        }
    }

    private static Neo4jPullWrapper createPullWrapper() {
        return new Neo4jPullWrapper(sessionProvider);
    }

    @Test
    public void readFromDB_DoesNotThrow() {
        assertDoesNotThrow(() -> {
            var inputWrapper = createPullWrapper();
            var dbContent = inputWrapper.readAllAsStringForTests();
            LOGGER.debug("DB content:\n" + dbContent);
        });
    }

    private void pullForestTestAlgorithm(String kindName, String expectedDataFileName, ComplexProperty accessPath) throws Exception {
        var inputWrapper = createPullWrapper();

        var forest = inputWrapper.pullForest(accessPath, new PullWrapperOptions.Builder().buildWithKindName(kindName));
        LOGGER.debug("Pulled forest:\n" + forest);

        var dummyWrapper = new DummyPullWrapper();
        var url = ClassLoader.getSystemResource("neo4j/" + expectedDataFileName);
        String fileName = Paths.get(url.toURI()).toAbsolutePath().toString();

        var expectedForest = dummyWrapper.pullForest(accessPath, new PullWrapperOptions.Builder().buildWithKindName(fileName));
        
        assertEquals(expectedForest, forest);
    }

    @Test
    public void getForestForNodeTest() throws Exception {
        pullForestTestAlgorithm("order", "1NodeTest.json", new TestData().path_neo4j_order());
    }

    @Test
    public void getForestForRelationshipTest() throws Exception {
        pullForestTestAlgorithm("items", "2RelationshipTest.json", new TestData().path_neo4j_items());
    }

    @Test
    public void testOfWrite() {
        var wrapper = new Neo4jControlWrapper(sessionProvider);

        assertDoesNotThrow(() -> {
            wrapper.execute(List.of(
                Neo4jStatement.createEmpty(),
                new Neo4jStatement("CREATE (a { test: '1' });")
            ));
        });

        List<AbstractStatement> invalidStatements = List.of(
            new Neo4jStatement("invalid statement")
        );

        assertThrows(ExecuteException.class, () -> {
            wrapper.execute(invalidStatements);
        });
    }

}
