package cz.matfyz.tests.transformations;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.UniqueIdGenerator;
import cz.matfyz.tests.example.basic.Schema;
import cz.matfyz.tests.example.basic.MongoDB;
import cz.matfyz.tests.example.basic.PostgreSQL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MTCAlgorithmTests {

    private static final SchemaCategory schema = Schema.newSchema();

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    @Test
    void basicTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(PostgreSQL.order(schema), """
                [ {
                    "number": "o_100"
                }, {
                    "number": "o_200"
                } ]
            """)
            .expected(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
            })
            .run();
    }

    @Test
    void structureTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.address(schema), """
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
            .expected(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addAddress(builder, 0, "0", "Ke Karlovu 2027/3", "Praha 2", "121 16");
                MongoDB.addAddress(builder, 1, "1", "Malostranské nám. 2/25", "Praha 1", "118 00");
            })
            .run();
    }

    @Test
    void simpleSetTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.tagSet(schema), """
                [ {
                    "number": "o_100",
                    "tags": [
                        "t_123",
                        "t_456",
                        "t_789"
                    ]
                }, {
                    "number": "o_200",
                    "tags": [
                        "t_123",
                        "t_555",
                        "t_888"
                    ]
                } ]
            """)
            .expected(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addTagSet(builder, 0, "t_123", "t_456", "t_789");
                MongoDB.addTagSet(builder, 1, "t_123", "t_555", "t_888");
            })
            .run();
    }

    @Test
    void simpleArrayTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.tagArray(schema), """
                [ {
                    "number": "o_100",
                    "tags": [
                        "t_123",
                        "t_456",
                        "t_789"
                    ]
                }, {
                    "number": "o_200",
                    "tags": [
                        "t_123",
                        "t_555",
                        "t_888"
                    ]
                } ]
            """)
            .expected(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addTagArray(builder, 0, "t_123", "t_456", "t_789");
                MongoDB.addTagArray(builder, 1, "t_123", "t_555", "t_888");
            })
            .run();
    }

    @Test
    void complexSetTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.item(schema), """
                [ {
                    "number": "o_100",
                    "items": [ {
                        "id": "p_123",
                        "label": "Clean Code",
                        "price": 125,
                        "quantity": 1
                    } ]
                }, {
                    "number": "o_100",
                    "items": [ {
                        "id": "p_765",
                        "label": "The Lord of the Rings",
                        "price": 199,
                        "quantity": 2
                    } ]
                }, {
                    "number": "o_200",
                    "items": [ {
                        "id": "p_457",
                        "label": "The Art of War",
                        "price": 299,
                        "quantity": 7
                    }, {
                        "id": "p_734",
                        "label": "Animal Farm",
                        "price": 350,
                        "quantity": 3
                    } ]
                } ]
            """)
            .expected(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                PostgreSQL.addProduct(builder, "p_123", "Clean Code", "125");
                PostgreSQL.addProduct(builder, "p_765", "The Lord of the Rings", "199");
                PostgreSQL.addProduct(builder, "p_457", "The Art of War", "299");
                PostgreSQL.addProduct(builder, "p_734", "Animal Farm", "350");
                MongoDB.addItem(builder, 0, 0, "1");
                MongoDB.addItem(builder, 0, 1, "2");
                MongoDB.addItem(builder, 1, 2, "7");
                MongoDB.addItem(builder, 1, 3, "3");
            })
            .run();
    }

    @Test
    void mapTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.contact(schema), """
                [ {
                    "number": "o_100",
                    "contact": {
                        "email": "anna@seznam.cz",
                        "cellphone": "+420777123456"
                    }
                }, {
                    "number": "o_200",
                    "contact": {
                        "skype": "skype123",
                        "cellphone": "+420123456789"
                    }
                } ]
            """)
            .expected(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addContact(builder, 0, "email", "anna@seznam.cz");
                MongoDB.addContact(builder, 0, "cellphone", "+420777123456");
                MongoDB.addContact(builder, 1, "skype", "skype123");
                MongoDB.addContact(builder, 1, "cellphone", "+420123456789");
            })
            .run();
    }

    @Test
    void auxiliaryPropertyTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.customer(schema), """
                [ {
                    "customer": {
                        "name": 1,
                        "number": "o_100"
                    }
                }, {
                    "customer": {
                        "name": 1,
                        "number": "o_200"
                    }
                } ]
            """)
            .expected(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addCustomer(builder, 0, "1");
                MongoDB.addCustomer(builder, 1, "1");
            })
            .run();
    }

    @Test
    void missingSimpleTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.address(schema), """
                [ {
                    "number": "o_100",
                    "address": {
                        "street": "Ke Karlovu 2027/3",
                        "city": null,
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
            .expected(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addAddress(builder, 0, "0", "Ke Karlovu 2027/3", null, "121 16");
                MongoDB.addAddress(builder, 1, "1", "Malostranské nám. 2/25", null, "118 00");
            })
            .run();
    }

    @Test
    void missingComplexTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.address(schema), """
                [ {
                    "number": "o_100",
                    "address": null
                }, {
                    "number": "o_200"
                } ]
            """)
            .expected(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
            })
            .run();
    }

    @Test
    void emptySetTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.item(schema), """
                [ {
                    "number": "o_100",
                    "items": null
                }, {
                    "number": "o_200",
                    "items": []
                } ]
            """)
            .expected(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
            })
            .run();
    }

    @Test
    void complexMapTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.note(schema), """
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
            .expected(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addNote(builder, 0, "cs-CZ", "0", "subject 1", "content cz");
                MongoDB.addNote(builder, 0, "en-US", "1", "subject 1", "content en");
                MongoDB.addNote(builder, 1, "cs-CZ", "3", "subject cz", "content 1");
                MongoDB.addNote(builder, 1, "en-GB", "2", "subject gb", "content 2");
            })
            .run();
    }

    @Test
    void missingSetTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.item(schema), """
                [ {
                    "number": "o_100",
                    "items": [ {
                        "id": "p_123",
                        "price": 125,
                        "quantity": 1
                    } ]
                }, {
                    "number": "o_100",
                    "items": [ {
                        "id": "p_765",
                        "label": "The Lord of the Rings",
                        "quantity": 2
                    } ]
                }, {
                    "number": "o_200",
                    "items": [ {
                        "id": "p_457",
                        "price": 299,
                        "quantity": 7
                    }, {
                        "id": "p_734",
                        "label": "Animal Farm",
                        "quantity": 3
                    } ]
                } ]
            """)
            .expected(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                PostgreSQL.addProduct(builder, "p_123", null, "125");
                PostgreSQL.addProduct(builder, "p_765", "The Lord of the Rings", null);
                PostgreSQL.addProduct(builder, "p_457", null, "299");
                PostgreSQL.addProduct(builder, "p_734", "Animal Farm", null);
                MongoDB.addItem(builder, 0, 0, "1");
                MongoDB.addItem(builder, 0, 1, "2");
                MongoDB.addItem(builder, 1, 2, "7");
                MongoDB.addItem(builder, 1, 3, "3");
            })
            .run();
    }

    @Test
    void multipleMappingsTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(PostgreSQL.order(schema), """
                [ {
                    "number": "o_100"
                }, {
                    "number": "o_200"
                } ]
            """)
            .mappingWithRecords(PostgreSQL.product(schema), """
                [ {
                    "id": "p_123",
                    "label": "Clean Code",
                    "price": "125"
                }, {
                    "id": "p_765",
                    "label": "The Lord of the Rings",
                    "price": "199"
                }, {
                    "id": "p_457",
                    "label": "The Art of War",
                    "price": "299"
                }, {
                    "id": "p_734",
                    "label": "Animal Farm",
                    "price": "350"
                } ]
            """)
            .mappingWithRecords(PostgreSQL.item(schema), """
                [ {
                    "order_number": "o_100",
                    "product_id": "p_123",
                    "quantity": 1
                }, {
                    "order_number": "o_100",
                    "product_id": "p_765",
                    "quantity": 2
                }, {
                    "order_number": "o_200",
                    "product_id": "p_457",
                    "quantity": 7
                }, {
                    "order_number": "o_200",
                    "product_id": "p_734",
                    "quantity": 3
                } ]
            """)
            .expected(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                PostgreSQL.addProduct(builder, "p_123", "Clean Code", "125");
                PostgreSQL.addProduct(builder, "p_765", "The Lord of the Rings", "199");
                PostgreSQL.addProduct(builder, "p_457", "The Art of War", "299");
                PostgreSQL.addProduct(builder, "p_734", "Animal Farm", "350");
                PostgreSQL.addItem(builder, 0, 0, "1");
                PostgreSQL.addItem(builder, 0, 1, "2");
                PostgreSQL.addItem(builder, 1, 2, "7");
                PostgreSQL.addItem(builder, 1, 3, "3");
            })
            .run();
    }

    @Test
    void dynamicNamesTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(PostgreSQL.dynamic(schema), """
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
            .expected(builder -> {
                PostgreSQL.addDynamic(builder, 0);
                PostgreSQL.addDynamic(builder, 1);
            })
            .run();
    }

    // TODO This test probably don't bring anything new as it just maps one value to another. However, an objex with multiple identifiers from which one is an EMPTY signature, might be more interesting.

    // @Test
    // void selfIdentifierTest() {
    //     final var data = new TestData();
    //     final var schemaV3 = data.createDefaultV3SchemaCategory();

    //     new MTCAlgorithmTestBase("12SelfIdentifierTest.json").setAll(
    //         schemaV3,
    //         data.orderKey,
    //         data.path_orderV3Root(),
    //         data.expectedInstance_selfIdentifier(schemaV3)
    //     )
    //         .run();
    // }

    // [ {
    //     "number": "o_100",
    //     "id": "#o_100"
    // }, {
    //     "number": "o_200",
    //     "id": "#o_200"
    // } ]

    @Test
    void hardcoreTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.hardcore(schema), """
                [ {
                    "id": "h_1",
                    "a": [ [
                        [ "v_a-00-0", "v_a-00-1" ],
                        [ "v_a-01-0", "v_a-01-1" ]
                    ], [
                        [ "v_a-10-0", "v_a-10-1" ],
                        [ "v_a-11-0", "v_a-11-1" ]
                    ] ],
                    "b": [ [
                        [ "v_b-00-0", "v_b-00-1" ],
                        [ "v_b-01-0", "v_b-01-1" ]
                    ], [
                        [ "v_b-10-0", "v_b-10-1" ],
                        [ "v_b-11-0", "v_b-11-1" ]
                    ] ],
                    "array": []
                }, {
                    "id": "h_2",
                    "array": [ {
                        "id": "c_0",
                        "x": { "i": "v_0-x-i", "j": "v_0-x-j" },
                        "y": { "i": "v_0-y-i", "j": "v_0-y-j" }
                    }, {
                        "id": "c_1",
                        "x": { "i": "v_1-x-i", "j": "v_1-x-j" },
                        "y": { "i": "v_1-y-i", "j": "v_1-y-j" }
                    } ],
                } ]
            """)
            .expected(builder -> MongoDB.addHardcore(builder))
            .run();
    }

}
