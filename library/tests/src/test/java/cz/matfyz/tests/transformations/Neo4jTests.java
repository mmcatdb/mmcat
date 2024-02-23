package cz.matfyz.tests.transformations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.exception.ExecuteException;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.basic.Databases;
import cz.matfyz.tests.example.basic.Neo4j;
import cz.matfyz.tests.example.common.TestDatabase;
import cz.matfyz.wrapperneo4j.Neo4jControlWrapper;
import cz.matfyz.wrapperneo4j.Neo4jStatement;

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

    private static final Databases databases = new Databases();
    private static final SchemaCategory schema = databases.schema;
    private static final TestDatabase<Neo4jControlWrapper> database = databases.neo4j();

    @BeforeAll
    public static void setup() {
        database.setup();
    }

    @Test
    public void readFromDB_DoesNotThrow() {
        assertDoesNotThrow(() -> {
            var inputWrapper = database.wrapper.getPullWrapper();
            var dbContent = inputWrapper.readNodeAsStringForTests(Neo4j.orderKind);
            LOGGER.debug("DB content:\n" + dbContent);
        });
    }

    @Test
    public void getForestForNodeTest() throws Exception {
        new PullForestTestBase(Neo4j.order(schema), database.wrapper.getPullWrapper())
            .expected("""
                [{
                    "customer": "Alice",
                    "number": "o_100"
                }, {
                    "customer": "Bob",
                    "number": "o_200"
                }]
            """)
            .run();
    }

    @Test
    public void getForestForRelationshipTest() throws Exception {
        new PullForestTestBase(Neo4j.item(schema), database.wrapper.getPullWrapper())
            .expected("""
                [{
                    "quantity": "10",
                    "_from.Order": {
                        "customer": "Alice"
                    },
                    "_to.Product": {
                        "id": "A1",
                        "label": "Some name"
                    }
                }, {
                    "quantity": "12",
                    "_from.Order": {
                        "customer": "Alice"
                    },
                    "_to.Product": {
                        "id": "A1",
                        "label": "Some name"
                    }
                }, {
                    "quantity": "17",
                    "_from.Order": {
                        "customer": "Alice"
                    },
                    "_to.Product": {
                        "id": "B2",
                        "label": "Other name"
                    }
                }, {
                    "quantity": "24",
                    "_from.Order": {
                        "customer": "Bob"
                    },
                    "_to.Product": {
                        "id": "B2",
                        "label": "Other name"
                    }
                }]
            """)
            .run();
    }

    @Test
    public void testOfWrite() {
        assertDoesNotThrow(() -> {
            database.wrapper.execute(List.of(
                Neo4jStatement.createEmpty(),
                new Neo4jStatement("CREATE (a:TestOfWrite { test: '1' });")
            ));
        });

        List<AbstractStatement> invalidStatements = List.of(
            new Neo4jStatement("invalid statement")
        );

        assertThrows(ExecuteException.class, () -> {
            database.wrapper.execute(invalidStatements);
        });
    }

}
