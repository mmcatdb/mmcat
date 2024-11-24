package cz.matfyz.tests.transformations;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.basic.Schema;
import cz.matfyz.tests.example.basic.MongoDB;
import cz.matfyz.tests.example.basic.PostgreSQL;

import org.junit.jupiter.api.Test;

class DDLAlgorithmTests {

    private static final SchemaCategory schema = Schema.newSchema();

    @Test
    void basicTest() {
        new DDLAlgorithmTestBase(PostgreSQL.order(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
            })
            .expected("""
                [
                    "setKindName(order)",
                    "isSchemaless()",
                    "addProperty(number, simple, required)",
                    "createDDLStatement()"
                ]
            """)
            .run();
    }

    @Test
    void structureTest() {
        new DDLAlgorithmTestBase(MongoDB.address(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addAddress(builder, 0, "0", "Ke Karlovu 2027/3", "Praha 2", "121 16");
                MongoDB.addAddress(builder, 1, "1", "Malostranské nám. 2/25", "Praha 1", "118 00");
            })
            .expected("""
                [
                    "setKindName(address)",
                    "isSchemaless()",
                    "addProperty(number, simple, required)",
                    "addProperty(address, complex, required)",
                    "addProperty(address/street, simple, required)",
                    "addProperty(address/city, simple, required)",
                    "addProperty(address/zip, simple, required)",
                    "createDDLStatement()"
                ]
            """)
            .run();
    }

    @Test
    void simpleArrayTest() {
        new DDLAlgorithmTestBase(MongoDB.tag(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addTag(builder, 0, new String[]{ "123", "456", "789" });
                MongoDB.addTag(builder, 1, new String[]{ "123", "String456", "String789" });
            })
            .expected("""
                [
                    "setKindName(tag)",
                    "isSchemaless()",
                    "addProperty(number, simple, required)",
                    "addProperty(tags[], simple, optional)",
                    "createDDLStatement()"
                ]
            """)
            .run();
    }

    @Test
    void complexArrayTest() {
        new DDLAlgorithmTestBase(MongoDB.item(schema))
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
                [
                    "setKindName(orderItem)",
                    "isSchemaless()",
                    "addProperty(number, simple, required)",
                    "addProperty(items[], complex, optional)",
                    "addProperty(items[]/id, simple, required)",
                    "addProperty(items[]/label, simple, optional)",
                    "addProperty(items[]/price, simple, optional)",
                    "addProperty(items[]/quantity, simple, required)",
                    "createDDLStatement()"
                ]
            """)
            .run();
    }

    @Test
    void mapTest() {
        new DDLAlgorithmTestBase(MongoDB.contact(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addContact(builder, 0, "email", "anna@seznam.cz");
                MongoDB.addContact(builder, 0, "cellphone", "+420777123456");
                MongoDB.addContact(builder, 1, "skype", "skype123");
                MongoDB.addContact(builder, 1, "cellphone", "+420123456789");
            })
            .expected("""
                [
                    "setKindName(contact)",
                    "isSchemaless()",
                    "addProperty(number, simple, required)",
                    "addProperty(contact, complex, required)",
                    "addProperty(contact/(cellphone|email|skype), simple, optional)",
                    "createDDLStatement()"
                ]
            """)
            .run();
    }

    @Test
    void syntheticPropertyTest() {
        new DDLAlgorithmTestBase(MongoDB.customer(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addCustomer(builder, 0, "1");
                MongoDB.addCustomer(builder, 1, "1");
            })
            .expected("""
                [
                    "setKindName(customer)",
                    "isSchemaless()",
                    "addProperty(customer, complex, required)",
                    "addProperty(customer/number, simple, required)",
                    "addProperty(customer/name, simple, required)",
                    "createDDLStatement()"
                ]
            """)
            .run();
    }

    @Test
    void missingSimpleTest() {
        new DDLAlgorithmTestBase(MongoDB.address(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addAddress(builder, 0, "0", "Ke Karlovu 2027/3", null, "121 16");
                MongoDB.addAddress(builder, 1, "1", "Malostranské nám. 2/25", null, "118 00");
            })
            .expected("""
                [
                    "setKindName(address)",
                    "isSchemaless()",
                    "addProperty(number, simple, required)",
                    "addProperty(address, complex, required)",
                    "addProperty(address/street, simple, required)",
                    "addProperty(address/city, simple, required)",
                    "addProperty(address/zip, simple, required)",
                    "createDDLStatement()"
                ]
            """)
            .run();
    }

    @Test
    void missingComplexTest() {
        new DDLAlgorithmTestBase(MongoDB.address(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
            })
            .expected("""
                [
                    "setKindName(address)",
                    "isSchemaless()",
                    "addProperty(number, simple, required)",
                    "addProperty(address, complex, required)",
                    "addProperty(address/street, simple, required)",
                    "addProperty(address/city, simple, required)",
                    "addProperty(address/zip, simple, required)",
                    "createDDLStatement()"
                ]
            """)
            .run();
    }

    @Test
    void emptyArrayTest() {
        new DDLAlgorithmTestBase(MongoDB.item(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
            })
            .expected("""
                [
                    "setKindName(orderItem)",
                    "isSchemaless()",
                    "addProperty(number, simple, required)",
                    "addProperty(items[], complex, optional)",
                    "addProperty(items[]/id, simple, required)",
                    "addProperty(items[]/label, simple, optional)",
                    "addProperty(items[]/price, simple, optional)",
                    "addProperty(items[]/quantity, simple, required)",
                    "createDDLStatement()"
                ]
            """)
            .run();
    }

    @Test
    void complexMapTest() {
        new DDLAlgorithmTestBase(MongoDB.note(schema))
            .instance(builder -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addNote(builder, 0, "cs-CZ", "0", "subject 1", "content cz");
                MongoDB.addNote(builder, 0, "en-US", "1", "subject 1", "content en");
                MongoDB.addNote(builder, 1, "cs-CZ", "3", "subject cz", "content 1");
                MongoDB.addNote(builder, 1, "en-GB", "2", "subject gb", "content 2");
            })
            .expected("""
                [
                    "setKindName(note)",
                    "isSchemaless()",
                    "addProperty(number, simple, required)",
                    "addProperty(note, complex, required)",
                    "addProperty(note/(cs-CZ|en-GB|en-US), complex, optional)",
                    "addProperty(note/(cs-CZ|en-GB|en-US)/subject, simple, required)",
                    "addProperty(note/(cs-CZ|en-GB|en-US)/content, simple, required)",
                    "createDDLStatement()"
                ]
            """)
            .run();
    }

    @Test
    void missingArrayTest() {
        new DDLAlgorithmTestBase(MongoDB.item(schema))
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
                [
                    "setKindName(orderItem)",
                    "isSchemaless()",
                    "addProperty(number, simple, required)",
                    "addProperty(items[], complex, optional)",
                    "addProperty(items[]/id, simple, required)",
                    "addProperty(items[]/label, simple, optional)",
                    "addProperty(items[]/price, simple, optional)",
                    "addProperty(items[]/quantity, simple, required)",
                    "createDDLStatement()"
                ]
            """)
            .run();
    }

    @Test
    void dynamicNamesTest() {
        new DDLAlgorithmTestBase(PostgreSQL.dynamic(schema))
            .instance(builder -> {
                PostgreSQL.addDynamic(builder, 0);
                PostgreSQL.addDynamic(builder, 1);
            })
            .expected("""
                [
                    "setKindName(dynamic)",
                    "isSchemaless()",
                    "addProperty(id, simple, required)",
                    "addProperty(label, simple, required)",
                    "addProperty((px_a|px_b), simple, optional)",
                    "addProperty((py_a|py_b), simple, optional)",
                    "addProperty((catch_all_a|catch_all_b), simple, optional)",
                    "createDDLStatement()"
                ]
            """)
            .run();
    }

    // TODO see MTC

    // @Test
    // void selfIdentifierTest() {
    //     final var data = new TestData();
    //     final var schemaV3 = data.createDefaultV3SchemaCategory();

    //     new DDLAlgorithmTestBase("12SelfIdentifierTest.json").setAll(
    //         schemaV3,
    //         data.orderKey,
    //         "order_v3",
    //         data.path_orderV3Root(),
    //         data.expectedInstance_selfIdentifier(schemaV3)
    //     )
    //         .run();
    // }

    // [
    //     "setKindName(order_v3)",
    //     "isSchemaless()",
    //     "addProperty(id, simple, required)",
    //     "addProperty(number, simple, required)",
    //     "createDDLStatement()"
    // ]

}
