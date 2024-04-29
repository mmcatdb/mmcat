package cz.matfyz.tests.transformations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.exception.ExecuteException;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.basic.Datasources;
import cz.matfyz.tests.example.basic.Neo4j;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.wrapperneo4j.Neo4jControlWrapper;
import cz.matfyz.wrapperneo4j.Neo4jStatement;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class Neo4jTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jTests.class);

    private static final Datasources datasources = new Datasources();
    private static final SchemaCategory schema = datasources.schema;
    private static final TestDatasource<Neo4jControlWrapper> datasource = datasources.neo4j();

    @BeforeAll
    static void setup() {
        datasource.setup();
    }

    @Test
    void readFromDB_DoesNotThrow() {
        assertDoesNotThrow(() -> {
            var inputWrapper = datasource.wrapper.getPullWrapper();
            var dbContent = inputWrapper.readNodeAsStringForTests(Neo4j.orderKind);
            LOGGER.debug("DB content:\n" + dbContent);
        });
    }

    @Test
    void getForestForNodeTest() throws Exception {
        new PullForestTestBase(Neo4j.order(schema), datasource.wrapper.getPullWrapper())
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
    void getForestForRelationshipTest() throws Exception {
        new PullForestTestBase(Neo4j.item(schema), datasource.wrapper.getPullWrapper())
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
    void testOfWrite() {
        assertDoesNotThrow(() -> {
            datasource.wrapper.execute(List.of(
                Neo4jStatement.createEmpty(),
                new Neo4jStatement("CREATE (a:TestOfWrite { test: '1' });")
            ));
        });

        List<AbstractStatement> invalidStatements = List.of(
            new Neo4jStatement("invalid statement")
        );

        assertThrows(ExecuteException.class, () -> {
            datasource.wrapper.execute(invalidStatements);
        });
    }

}
