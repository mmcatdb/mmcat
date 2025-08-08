package cz.matfyz.tests.transformations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.basic.Datasources;
import cz.matfyz.tests.example.basic.PostgreSQL;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PostgreSQLTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLTests.class);

    private static final Datasources datasources = new Datasources();
    private static final SchemaCategory schema = datasources.schema;
    private static final TestDatasource<PostgreSQLControlWrapper> datasource = datasources.postgreSQL();

    @BeforeAll
    static void setup() {
        datasource.setup();
    }

    @Test
    void readFromDB_DoesNotThrow() {
        assertDoesNotThrow(() -> {
            var inputWrapper = datasource.wrapper.getPullWrapper();
            var dbContent = inputWrapper.readTableAsStringForTests(PostgreSQL.orderKind);
            LOGGER.debug("DB content:\n" + dbContent);
        });
    }

    @Test
    void getForestForBasicTest() throws Exception {
        new PullForestTestBase(PostgreSQL.order(schema), datasource.wrapper.getPullWrapper())
            .expected("""
                [ {
                    "number": "o_100"
                }, {
                    "number": "o_200"
                } ]
            """)
            .run();
    }

    @Test
    void getForestForStructureTest() throws Exception {
        new PullForestTestBase(PostgreSQL.product(schema), datasource.wrapper.getPullWrapper())
            .expected("""
                [ {
                    "id": "123",
                    "label": "Clean Code",
                    "price": "125"
                }, {
                    "id": "765",
                    "label": "The Lord of the Rings",
                    "price": "199"
                }, {
                    "id": "457",
                    "label": "The Art of War",
                    "price": "299"
                }, {
                    "id": "734",
                    "label": "Animal Farm",
                    "price": "350"
                } ]
            """)
            .run();
    }

    @Test
    void getForestForDynamicNamesTest() throws Exception {
        new PullForestTestBase(PostgreSQL.dynamic(schema), datasource.wrapper.getPullWrapper())
            .expected("""
                [ {
                    "id": "id-0",
                    "label": "label-0",
                    "px_a": "px-a-0",
                    "py_a": "py-a-0",
                    "px_b": "px-b-0",
                    "py_b": "py-b-0",
                    "catch_all_a": "catch-all-a-0",
                    "catch_all_b": "catch-all-b-0"
                }, {
                    "id": "id-1",
                    "label": "label-1",
                    "px_a": "px-a-1",
                    "py_a": "py-a-1",
                    "px_b": "px-b-1",
                    "py_b": "py-b-1",
                    "catch_all_a": "catch-all-a-1",
                    "catch_all_b": "catch-all-b-1"
                } ]
            """)
            .run();
    }

    /*
    @Test
    void getForestForSimpleArrayTest() throws Exception {
        pullForestTestAlgorithm("simple_array", "3SimpleArrayTest.json", new Tag(null));
    }

    @Test
    void getForestForComplexArrayTest() throws Exception {
        pullForestTestAlgorithm("complex_array", "4ComplexArrayTest.json", new Item(null));
    }

    @Test
    void getForestForEmptyArrayTest() throws Exception {
        pullForestTestAlgorithm("empty_array", "9EmptyArrayTest.json", new Item(null));
    }

    @Test
    void getForestForComplexMapTest() throws Exception {
        pullForestTestAlgorithm("complex_map", "10ComplexMapTest.json", new Note(null));
    }
    */
}
