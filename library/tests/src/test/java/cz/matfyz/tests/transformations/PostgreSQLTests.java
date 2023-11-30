package cz.matfyz.tests.transformations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.basic.Databases;
import cz.matfyz.tests.example.basic.PostgreSQL;
import cz.matfyz.tests.example.common.TestDatabase;
import cz.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLTests.class);

    private static final Databases databases = new Databases();
    private static final SchemaCategory schema = databases.schema;
    private static final TestDatabase<PostgreSQLControlWrapper> database = databases.postgreSQL();

    @BeforeAll
    public static void setup() {
        database.setup();
    }

    @Test
    public void readFromDB_DoesNotThrow() {
        assertDoesNotThrow(() -> {
            var inputWrapper = database.wrapper.getPullWrapper();
            var dbContent = inputWrapper.readTableAsStringForTests("SELECT * FROM \"order\";");
            LOGGER.debug("DB content:\n" + dbContent);
        });
    }

    @Test
    public void getForestForBasicTest() throws Exception {
        new PullForestTestBase(PostgreSQL.order(schema), database.wrapper.getPullWrapper())
            .expected("""
                [{
                    "number": "o_100"
                }, {
                    "number": "o_200"
                }]        
            """)
            .run();
    }

    @Test
    public void getForestForStructureTest() throws Exception {
        new PullForestTestBase(PostgreSQL.product(schema), database.wrapper.getPullWrapper())
            .expected("""
                [{
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
                }]       
            """)
            .run();
    }

    /*
    @Test
    public void getForestForSimpleArrayTest() throws Exception {
        pullForestTestAlgorithm("simple_array", "3SimpleArrayTest.json", new Tag(null));
    }

    @Test
    public void getForestForComplexArrayTest() throws Exception {
        pullForestTestAlgorithm("complex_array", "4ComplexArrayTest.json", new Item(null));
    }

    @Test
    public void getForestForEmptyArrayTest() throws Exception {
        pullForestTestAlgorithm("empty_array", "9EmptyArrayTest.json", new Item(null));
    }

    @Test
    public void getForestForComplexMapTest() throws Exception {
        pullForestTestAlgorithm("complex_map", "10ComplexMapTest.json", new Note(null));
    }
    */
}
