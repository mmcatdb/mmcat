package cz.matfyz.tests.transformations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.basic.Datasources;
import cz.matfyz.tests.example.basic.MongoDB;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBTests.class);

    private static final Datasources datasources = new Datasources();
    private static final SchemaCategory schema = datasources.schema;
    private static final TestDatasource<MongoDBControlWrapper> datasource = datasources.mongoDB();

    @BeforeAll
    public static void setup() {
        datasource.setup();
    }

    @Test
    void readFromDB_DoesNotThrow() {
        assertDoesNotThrow(() -> {
            final var inputWrapper = datasource.wrapper.getPullWrapper();
            final var dbContent = inputWrapper.readCollectionAsStringForTests(MongoDB.orderKind);
            LOGGER.trace("DB content:\n" + dbContent);
        });
    }

    @Test
    void getForestForBasicTest() throws Exception {
        new PullForestTestBase(MongoDB.order(schema), datasource.wrapper.getPullWrapper())
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
        new PullForestTestBase(MongoDB.address(schema), datasource.wrapper.getPullWrapper())
            .expected("""
                [ {
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
                } ]
            """)
            .run();
    }

    @Test
    void getForestForSimpleArrayTest() throws Exception {
        new PullForestTestBase(MongoDB.tag(schema), datasource.wrapper.getPullWrapper())
            .expected("""
                [ {
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
                } ]
            """)
            .run();
    }

    @Test
    void getForestForComplexArrayTest() throws Exception {
        new PullForestTestBase(MongoDB.item(schema), datasource.wrapper.getPullWrapper())
            .expected("""
                [ {
                    "number": "o_100",
                    "items": [ {
                        "id": 123,
                        "label": "Clean Code",
                        "price": 125,
                        "quantity": 1
                    } ]
                }, {
                    "number": "o_100",
                    "items": [ {
                        "id": 765,
                        "label": "The Lord of the Rings",
                        "price": 199,
                        "quantity": 2
                    } ]
                }, {
                    "number": "o_200",
                    "items": [ {
                        "id": 457,
                        "label": "The Art of War",
                        "price": 299,
                        "quantity": 7
                    }, {
                        "id": 734,
                        "label": "Animal Farm",
                        "price": 350,
                        "quantity": 3
                    } ]
                } ]
            """)
            .run();
    }

    @Test
    void getForestForEmptyArrayTest() throws Exception {
        new PullForestTestBase(MongoDB.itemEmpty(schema), datasource.wrapper.getPullWrapper())
            .expected("""
                [ {
                    "number": "o_100",
                    "items": null
                }, {
                    "number": "o_200",
                    "items": []
                } ]
            """)
            .run();
    }

    @Test
    void getForestForComplexMapTest() throws Exception {
        new PullForestTestBase(MongoDB.note(schema), datasource.wrapper.getPullWrapper())
            .expected("""
                [ {
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
                } ]
            """)
            .run();
    }

    @Test
    void getForestForMissingSimpleTest() throws Exception {
        new PullForestTestBase(MongoDB.addressMissingSimple(schema), datasource.wrapper.getPullWrapper())
            .expected("""
                [ {
                    "number": "o_100",
                    "address": {
                        "street": "Ke Karlovu 2027/3",
                        "zip": "121 16"
                    }
                }, {
                    "number": "o_200",
                    "address": {
                        "street": "Malostranské nám. 2/25",
                        "zip": "118 00"
                    }
                } ]
            """)
            .run();
    }

    @Test
    void getForestForMissingComplexTest() throws Exception {
        new PullForestTestBase(MongoDB.addressMissingComplex(schema), datasource.wrapper.getPullWrapper())
            .expected("""
                [ {
                    "number": "o_100",
                }, {
                    "number": "o_200"
                } ]
            """)
            .run();
    }

}
