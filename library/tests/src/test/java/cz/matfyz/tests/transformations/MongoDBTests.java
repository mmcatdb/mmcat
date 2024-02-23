package cz.matfyz.tests.transformations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.basic.Databases;
import cz.matfyz.tests.example.basic.MongoDB;
import cz.matfyz.tests.example.common.TestDatabase;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author jachymb.bartik
 */
public class MongoDBTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBTests.class);

    private static final Databases databases = new Databases();
    private static final SchemaCategory schema = databases.schema;
    private static final TestDatabase<MongoDBControlWrapper> database = databases.mongoDB();

    @BeforeAll
    public static void setup() {
        database.setup();
    }

    @Test
    void readFromDB_DoesNotThrow() {
        assertDoesNotThrow(() -> {
            var inputWrapper = database.wrapper.getPullWrapper();
            var dbContent = inputWrapper.readCollectionAsStringForTests(MongoDB.orderKind);
            LOGGER.trace("DB content:\n" + dbContent);
        });
    }

    @Test
    void getForestForBasicTest() throws Exception {
        new PullForestTestBase(MongoDB.order(schema), database.wrapper.getPullWrapper())
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
    void getForestForStructureTest() throws Exception {
        new PullForestTestBase(MongoDB.address(schema), database.wrapper.getPullWrapper())
            .expected("""
                [{
                    "number": "o_100",
                    "address": {
                        "street": "Ke Karlovu 2027/3",
                        "city": "Praha 2",
                        "zip": "121 16"
                    }
                }, {
                    "number": "o_200",
                    "address": {
                        "street": "Malostranské nám. 2/25",
                        "city": "Praha 1",
                        "zip": "118 00"
                    }
                }]
            """)
            .run();
    }

    @Test
    void getForestForSimpleArrayTest() throws Exception {
        new PullForestTestBase(MongoDB.tag(schema), database.wrapper.getPullWrapper())
            .expected("""
                [{
                    "number": "o_100",
                    "tags": [
                        123,
                        456,
                        789
                    ]
                }, {
                    "number": "o_200",
                    "tags": [
                        "123",
                        "String456",
                        "String789"
                    ]
                }]
            """)
            .run();
    }

    @Test
    void getForestForComplexArrayTest() throws Exception {
        new PullForestTestBase(MongoDB.item(schema), database.wrapper.getPullWrapper())
            .expected("""
                [{
                    "number": "o_100",
                    "items": [
                        {
                            "id": 123,
                            "label": "Clean Code",
                            "price": 125,
                            "quantity": 1
                        }
                    ]
                }, {
                    "number": "o_100",
                    "items": [
                        {
                            "id": 765,
                            "label": "The Lord of the Rings",
                            "price": 199,
                            "quantity": 2
                        }
                    ]
                }, {
                    "number": "o_200",
                    "items": [
                        {
                            "id": 457,
                            "label": "The Art of War",
                            "price": 299,
                            "quantity": 7
                        },
                        {
                            "id": 734,
                            "label": "Animal Farm",
                            "price": 350,
                            "quantity": 3
                        }
                    ]
                }]
            """)
            .run();
    }

    @Test
    void getForestForEmptyArrayTest() throws Exception {
        new PullForestTestBase(MongoDB.itemEmpty(schema), database.wrapper.getPullWrapper())
            .expected("""
                [{
                    "number": "o_100",
                    "items": null
                }, {
                    "number": "o_200",
                    "items": []
                }]
            """)
            .run();
    }

    @Test
    void getForestForComplexMapTest() throws Exception {
        new PullForestTestBase(MongoDB.note(schema), database.wrapper.getPullWrapper())
            .expected("""
                [{
                    "number": "o_100",
                    "note": {
                        "cs-CZ": {
                            "subject": "subject 1",
                            "content": "content cz"
                        },
                        "en-US": {
                            "subject": "subject 1",
                            "content": "content en"
                        }
                    }
                }, {
                    "number": "o_200",
                    "note": {
                        "cs-CZ": {
                            "subject": "subject cz",
                            "content": "content 1"
                        },
                        "en-GB": {
                            "subject": "subject gb",
                            "content": "content 2"
                        }
                    }
                }]
            """)
            .run();
    }
}
