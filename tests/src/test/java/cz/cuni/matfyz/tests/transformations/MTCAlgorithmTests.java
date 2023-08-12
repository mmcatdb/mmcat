package cz.cuni.matfyz.tests.transformations;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.utils.UniqueIdProvider;
import cz.cuni.matfyz.tests.mapping.MongoDB;
import cz.cuni.matfyz.tests.mapping.PostgreSQL;
import cz.cuni.matfyz.tests.schema.TestSchema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author jachymb.bartik
 */
public class MTCAlgorithmTests {

    private static final SchemaCategory schema = TestSchema.newSchemaCategory();
    
    @BeforeEach
    public void setUp() {
        UniqueIdProvider.reset();
    }

    @Test
    public void basicTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(PostgreSQL.order(schema), """
                [{
                    "number": "o_100"
                }, {
                    "number": "o_200"
                }]
            """)
            .expected((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
            })
            .run();
    }

    @Test
    public void structureTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.address(schema), """
                [{
                    "number": "o_100",
                    "address": {
                        "street": "hodnotaA",
                        "city": "hodnotaB",
                        "zip": "hodnotaC"
                    }
                }, {
                    "number": "o_200",
                    "address": {
                        "street": "hodnotaA2",
                        "city": "hodnotaB2",
                        "zip": "hodnotaC2"
                    }
                }]
            """)
            .expected((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addAddress(builder, 0, "0", "hodnotaA", "hodnotaB", "hodnotaC");
                MongoDB.addAddress(builder, 1, "1", "hodnotaA2", "hodnotaB2", "hodnotaC2");
            })
            .run();
    }

    @Test
    public void simpleArrayTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.tag(schema), """
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
            .expected((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addTag(builder, 0, new String[]{ "123", "456", "789" });
                MongoDB.addTag(builder, 1, new String[]{ "123", "String456", "String789" });
            })
            .run();
    }

    @Test
    public void complexArrayTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.item(schema), """
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
            .expected((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                PostgreSQL.addProduct(builder, "123", "Clean Code", "125");
                PostgreSQL.addProduct(builder, "765", "The Lord of the Rings", "199");
                PostgreSQL.addProduct(builder, "457", "The Art of War", "299");
                PostgreSQL.addProduct(builder, "734", "Animal Farm", "350");
                MongoDB.addItem(builder, 0, 0, "1");
                MongoDB.addItem(builder, 0, 1, "2");
                MongoDB.addItem(builder, 1, 2, "7");
                MongoDB.addItem(builder, 1, 3, "3");
            })
            .run();
    }

    @Test
    public void mapTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.contact(schema), """
                [{
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
                }]
            """)
            .expected((builder) -> {
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
    public void syntheticPropertyTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.customer(schema), """
                [{
                    "customer": {
                        "name": 1,
                        "number": "o_100"
                    }
                }, {
                    "customer": {
                        "name": 1,
                        "number": "o_200"
                    }
                }]
            """)
            .expected((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addCustomer(builder, 0, "1");
                MongoDB.addCustomer(builder, 1, "1");
            })
            .run();
    }

    @Test
    public void missingSimpleTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.address(schema), """
                [{
                    "number": "o_100",
                    "address": {
                        "street": "hodnotaA",
                        "city": null,
                        "zip": "hodnotaC"
                    }
                }, {
                    "number": "o_200",
                    "address": {
                        "street": "hodnotaA2",
                        "zip": "hodnotaC2"
                    }
                }]
            """)
            .expected((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addAddress(builder, 0, "0", "hodnotaA", null, "hodnotaC");
                MongoDB.addAddress(builder, 1, "1", "hodnotaA2", null, "hodnotaC2");
            })
            .run();
    }

    @Test
    public void missingComplexTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.address(schema), """
                [{
                    "number": "o_100",
                    "nested": null
                }, {
                    "number": "o_200"
                }]
            """)
            .expected((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
            })
            .run();
    }

    @Test
    public void emptyArrayTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.item(schema), """
                [{
                    "number": "o_100",
                    "items": null
                }, {
                    "number": "o_200",
                    "items": []
                }]
            """)
            .expected((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
            })
            .run();
    }

    @Test
    public void complexMapTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.note(schema), """
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
            .expected((builder) -> {
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
    public void missingArrayTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(MongoDB.item(schema), """
                [{
                    "number": "o_100",
                    "items": [
                        {
                            "id": 123,
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
                            "quantity": 2
                        }
                    ]
                }, {
                    "number": "o_200",
                    "items": [
                        {
                            "id": 457,
                            "price": 299,
                            "quantity": 7
                        },
                        {
                            "id": 734,
                            "label": "Animal Farm",
                            "quantity": 3
                        }
                    ]
                }]
            """)
            .expected((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                PostgreSQL.addProduct(builder, "123", null, "125");
                PostgreSQL.addProduct(builder, "765", "The Lord of the Rings", null);
                PostgreSQL.addProduct(builder, "457", null, "299");
                PostgreSQL.addProduct(builder, "734", "Animal Farm", null);
                MongoDB.addItem(builder, 0, 0, "1");
                MongoDB.addItem(builder, 0, 1, "2");
                MongoDB.addItem(builder, 1, 2, "7");
                MongoDB.addItem(builder, 1, 3, "3");
            })
            .run();
    }

    @Test
    public void multipleMappingsTest() {
        new MTCAlgorithmTestBase()
            .mappingWithRecords(PostgreSQL.order(schema), """
                [{
                    "number": "o_100"
                }, {
                    "number": "o_200"
                }]
            """)
            .mappingWithRecords(PostgreSQL.product(schema), """
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
            .mappingWithRecords(PostgreSQL.item(schema), """
                [{
                    "order_number": "o_100",
                    "product_id": "123",
                    "quantity": 1
                }, {
                    "order_number": "o_100",
                    "product_id": "765",
                    "quantity": 2
                }, {
                    "order_number": "o_200",
                    "product_id": "457",
                    "quantity": 7
                }, {
                    "order_number": "o_200",
                    "product_id": "734",
                    "quantity": 3
                }]
            """)
            .expected((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                PostgreSQL.addProduct(builder, "123", "Clean Code", "125");
                PostgreSQL.addProduct(builder, "765", "The Lord of the Rings", "199");
                PostgreSQL.addProduct(builder, "457", "The Art of War", "299");
                PostgreSQL.addProduct(builder, "734", "Animal Farm", "350");
                PostgreSQL.addItem(builder, 0, 0, "1");
                PostgreSQL.addItem(builder, 0, 1, "2");
                PostgreSQL.addItem(builder, 1, 2, "7");
                PostgreSQL.addItem(builder, 1, 3, "3");
            })
            .run();
    }

    // TODO This test probably don't bring anything new as it just maps one value to another. However, an object with multiple identifiers from which one is an EMPTY signature, might be more interesting.

    // @Test
    // public void selfIdentifierTest() {
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

    // [{
    //     "number": "o_100",
    //     "id": "#o_100"
    // }, {
    //     "number": "o_200",
    //     "id": "#o_200"
    // }]
    
}
