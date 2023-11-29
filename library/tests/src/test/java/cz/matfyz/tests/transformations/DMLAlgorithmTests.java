package cz.matfyz.tests.transformations;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.mapping.MongoDB;
import cz.matfyz.tests.mapping.PostgreSQL;
import cz.matfyz.tests.schema.BasicSchema;

import org.junit.jupiter.api.Test;

/**
 * @author jachymb.bartik
 */
public class DMLAlgorithmTests {

    private static final SchemaCategory schema = BasicSchema.newSchemaCategory();
    
    @Test
    public void basicTest() {
        new DMLAlgorithmTestBase(PostgreSQL.order(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
            })
            .expected("""
                [{
                    "name": "order",
                    "values": [
                        "append(number, o_100)"
                    ]
                }, {
                    "name": "order",
                    "values": [
                        "append(number, o_200)"
                    ]
                }]
            """)
            .run();
    }

    @Test
    public void structureTest() {
        new DMLAlgorithmTestBase(MongoDB.address(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addAddress(builder, 0, "0", "hodnotaA", "hodnotaB", "hodnotaC");
                MongoDB.addAddress(builder, 1, "1", "hodnotaA2", "hodnotaB2", "hodnotaC2");
            })
            .expected("""
                [{
                    "name": "order",
                    "values": [
                        "append(number, o_100)",
                        "append(address/street, hodnotaA)",
                        "append(address/city, hodnotaB)",
                        "append(address/zip, hodnotaC)"
                    ]
                }, {
                    "name": "order",
                    "values": [
                        "append(number, o_200)",
                        "append(address/street, hodnotaA2)",
                        "append(address/city, hodnotaB2)",
                        "append(address/zip, hodnotaC2)"
                    ]
                }]
            """)
            .run();
    }

    @Test
    public void simpleArrayTest() {
        new DMLAlgorithmTestBase(MongoDB.tag(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addTag(builder, 0, new String[]{ "123", "456", "789" });
                MongoDB.addTag(builder, 1, new String[]{ "123", "String456", "String789" });
            })
            .expected("""
                [{
                    "name": "order",
                    "values": [
                        "append(number, o_100)",
                        "append(tags[0], 123)",
                        "append(tags[1], 456)",
                        "append(tags[2], 789)"
                    ]
                }, {
                    "name": "order",
                    "values": [
                        "append(number, o_200)",
                        "append(tags[0], 123)",
                        "append(tags[1], String456)",
                        "append(tags[2], String789)"
                    ]
                }]
            """)
            .run();
    }

    // This isn't a comprehensive test because it doesn't check if the order is correct. However, to implement a complete test would be such an overkill.
    @Test
    public void complexArrayTest() {
        new DMLAlgorithmTestBase(MongoDB.item(schema))
            .instance(builder -> {
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
            .expected("""
                [{
                    "name": "orderItem",
                    "values": [
                        "append(number, o_100)",
                        "append(items[0]/id, 123)",
                        "append(items[1]/id, 765)",
                        "append(items[0]/label, Clean Code)",
                        "append(items[1]/label, The Lord of the Rings)",
                        "append(items[0]/price, 125)",
                        "append(items[1]/price, 199)",
                        "append(items[0]/quantity, 1)",
                        "append(items[1]/quantity, 2)"
                    ]
                }, {
                    "name": "orderItem",
                    "values": [
                        "append(number, o_200)",
                        "append(items[0]/id, 457)",
                        "append(items[1]/id, 734)",
                        "append(items[0]/label, The Art of War)",
                        "append(items[1]/label, Animal Farm)",
                        "append(items[0]/price, 299)",
                        "append(items[1]/price, 350)",
                        "append(items[0]/quantity, 7)",
                        "append(items[1]/quantity, 3)"
                    ]
                }]
            """)
            .run();
    }

    @Test
    public void mapTest() {
        new DMLAlgorithmTestBase(MongoDB.contact(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addContact(builder, 0, "email", "anna@seznam.cz");
                MongoDB.addContact(builder, 0, "cellphone", "+420777123456");
                MongoDB.addContact(builder, 1, "skype", "skype123");
                MongoDB.addContact(builder, 1, "cellphone", "+420123456789");
            })
            .expected("""
                [{
                    "name": "order",
                    "values": [
                        "append(number, o_100)",
                        "append(contact/email, anna@seznam.cz)",
                        "append(contact/cellphone, +420777123456)"
                    ]
                }, {
                    "name": "order",
                    "values": [
                        "append(number, o_200)",
                        "append(contact/skype, skype123)",
                        "append(contact/cellphone, +420123456789)"
                    ]
                }]
            """)
            .run();
    }

    @Test
    public void syntheticPropertyTest() {
        new DMLAlgorithmTestBase(MongoDB.customer(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addCustomer(builder, 0, "1");
                MongoDB.addCustomer(builder, 1, "1");
            })
            .expected("""
                [{
                    "name": "order",
                    "values": [
                        "append(customer/number, o_100)",
                        "append(customer/name, 1)"
                    ]
                }, {
                    "name": "order",
                    "values": [
                        "append(customer/number, o_200)",
                        "append(customer/name, 1)"
                    ]
                }]
            """)
            .run();
    }

    @Test
    public void missingSimpleTest() {
        new DMLAlgorithmTestBase(MongoDB.address(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addAddress(builder, 0, "0", "hodnotaA", null, "hodnotaC");
                MongoDB.addAddress(builder, 1, "1", "hodnotaA2", null, "hodnotaC2");
            })
            .expected("""
                [{
                    "name": "order",
                    "values": [
                        "append(number, o_100)",
                        "append(address/street, hodnotaA)",
                        "append(address/zip, hodnotaC)"
                    ]
                }, {
                    "name": "order",
                    "values": [
                        "append(number, o_200)",
                        "append(address/street, hodnotaA2)",
                        "append(address/zip, hodnotaC2)"
                    ]
                }]
            """)
            .run();
    }

    @Test
    public void missingComplexTest() {
        new DMLAlgorithmTestBase(MongoDB.address(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
            })
            .expected("""
                [{
                    "name": "order",
                    "values": [
                        "append(number, o_100)"
                    ]
                }, {
                    "name": "order",
                    "values": [
                        "append(number, o_200)"
                    ]
                }]
            """)
            .run();
    }

    @Test
    public void emptyArrayTest() {
        new DMLAlgorithmTestBase(MongoDB.item(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
            })
            .expected("""
                [{
                    "name": "orderItem",
                    "values": [
                        "append(number, o_100)",
                        "append(items, null)"
                    ]
                }, {
                    "name": "orderItem",
                    "values": [
                        "append(number, o_200)",
                        "append(items, null)"
                    ]
                }]
            """)
            .run();
    }

    @Test
    public void complexMapTest() {
        new DMLAlgorithmTestBase(MongoDB.note(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addNote(builder, 0, "cs-CZ", "0", "subject 1", "content cz");
                MongoDB.addNote(builder, 0, "en-US", "1", "subject 1", "content en");
                MongoDB.addNote(builder, 1, "cs-CZ", "3", "subject cz", "content 1");
                MongoDB.addNote(builder, 1, "en-GB", "2", "subject gb", "content 2");
            })
            .expected("""
                [{
                    "name": "order",
                    "values": [
                        "append(number, o_100)",
                        "append(note/cs-CZ/subject, subject 1)",
                        "append(note/cs-CZ/content, content cz)",
                        "append(note/en-US/subject, subject 1)",
                        "append(note/en-US/content, content en)"
                    ]
                }, {
                    "name": "order",
                    "values": [
                        "append(number, o_200)",
                        "append(note/cs-CZ/subject, subject cz)",
                        "append(note/cs-CZ/content, content 1)",
                        "append(note/en-GB/subject, subject gb)",
                        "append(note/en-GB/content, content 2)"
                    ]
                }]
            """)
            .run();
    }

    @Test
    public void missingArrayTest() {
        new DMLAlgorithmTestBase(MongoDB.item(schema))
            .instance(builder -> {
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
            .expected("""
                [{
                    "name": "orderItem",
                    "values": [
                        "append(number, o_100)",
                        "append(items[0]/id, 123)",
                        "append(items[1]/id, 765)",
                        "append(items[1]/label, The Lord of the Rings)",
                        "append(items[0]/price, 125)",
                        "append(items[0]/quantity, 1)",
                        "append(items[1]/quantity, 2)"
                    ]
                }, {
                    "name": "orderItem",
                    "values": [
                        "append(number, o_200)",
                        "append(items[0]/id, 457)",
                        "append(items[1]/id, 734)",
                        "append(items[1]/label, Animal Farm)",
                        "append(items[0]/price, 299)",
                        "append(items[0]/quantity, 7)",
                        "append(items[1]/quantity, 3)"
                    ]
                }]
            """)
            .run();
    }

    // TODO see MTC

    // @Test
    // public void selfIdentifierTest() {
    //     final var data = new TestData();
    //     final var schemaV3 = data.createDefaultV3SchemaCategory();

    //     new DMLAlgorithmTestBase("12SelfIdentifierTest.json").setAll(
    //         schemaV3,
    //         data.orderKey,
    //         "order_v3",
    //         data.path_orderV3Root(),
    //         data.expectedInstance_selfIdentifier(schemaV3)
    //     )
    //         .run();
    // }

    // [{
    //     "name": "order_v3",
    //     "values": [
    //         "append(id, #o_100)",
    //         "append(number, o_100)"
    //     ]
    // }, {
    //     "name": "order_v3",
    //     "values": [
    //         "append(id, #o_200)",
    //         "append(number, o_200)"
    //     ]
    // }]
    
}
