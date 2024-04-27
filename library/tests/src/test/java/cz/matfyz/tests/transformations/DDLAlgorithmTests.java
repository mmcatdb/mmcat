package cz.matfyz.tests.transformations;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.basic.Schema;
import cz.matfyz.tests.example.basic.MongoDB;
import cz.matfyz.tests.example.basic.PostgreSQL;

import org.junit.jupiter.api.Test;

class DDLAlgorithmTests {

    private static final SchemaCategory schema = Schema.newSchemaCategory();

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
                    "isSchemaLess()",
                    "createDDLStatement()",
                    "addSimpleProperty([ number ], true)"
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
                    "isSchemaLess()",
                    "createDDLStatement()",
                    "addSimpleProperty([ number ], true)",
                    "addComplexProperty([ address ], true)",
                    "addSimpleProperty([ address/street ], true)",
                    "addSimpleProperty([ address/city ], true)",
                    "addSimpleProperty([ address/zip ], true)"
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
                    "isSchemaLess()",
                    "createDDLStatement()",
                    "addSimpleProperty([ number ], true)",
                    "addSimpleArrayProperty([ tags ], false)"
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
                    "isSchemaLess()",
                    "createDDLStatement()",
                    "addSimpleProperty([ number ], true)",
                    "addComplexArrayProperty([ items ], false)",
                    "addSimpleProperty([ items/id ], true)",
                    "addSimpleProperty([ items/label ], false)",
                    "addSimpleProperty([ items/price ], false)",
                    "addSimpleProperty([ items/quantity ], true)"
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
                    "isSchemaLess()",
                    "createDDLStatement()",
                    "addSimpleProperty([ number ], true)",
                    "addComplexProperty([ contact ], false)",
                    "addSimpleProperty([ contact/cellphone, contact/email, contact/skype ], true)"
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
                    "isSchemaLess()",
                    "createDDLStatement()",
                    "addSimpleProperty([ customer/number ], true)",
                    "addSimpleProperty([ customer/name ], true)"
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
                    "isSchemaLess()",
                    "createDDLStatement()",
                    "addSimpleProperty([ number ], true)",
                    "addComplexProperty([ address ], true)",
                    "addSimpleProperty([ address/street ], true)",
                    "addSimpleProperty([ address/city ], true)",
                    "addSimpleProperty([ address/zip ], true)"
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
                    "isSchemaLess()",
                    "createDDLStatement()",
                    "addSimpleProperty([ number ], true)",
                    "addComplexProperty([ address ], true)",
                    "addSimpleProperty([ address/street ], true)",
                    "addSimpleProperty([ address/city ], true)",
                    "addSimpleProperty([ address/zip ], true)"
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
                    "isSchemaLess()",
                    "createDDLStatement()",
                    "addSimpleProperty([ number ], true)",
                    "addComplexArrayProperty([ items ], false)",
                    "addSimpleProperty([ items/id ], true)",
                    "addSimpleProperty([ items/label ], false)",
                    "addSimpleProperty([ items/price ], false)",
                    "addSimpleProperty([ items/quantity ], true)"
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
                    "isSchemaLess()",
                    "createDDLStatement()",
                    "addSimpleProperty([ number ], true)",
                    "addComplexProperty([ note ], false)",
                    "addComplexProperty([ note/cs-CZ, note/en-GB, note/en-US ], true)",
                    "addSimpleProperty([ note/cs-CZ/subject, note/en-GB/subject, note/en-US/subject ], true)",
                    "addSimpleProperty([ note/cs-CZ/content, note/en-GB/content, note/en-US/content ], true)"
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
                    "isSchemaLess()",
                    "createDDLStatement()",
                    "addSimpleProperty([ number ], true)",
                    "addComplexArrayProperty([ items ], false)",
                    "addSimpleProperty([ items/id ], true)",
                    "addSimpleProperty([ items/label ], false)",
                    "addSimpleProperty([ items/price ], false)",
                    "addSimpleProperty([ items/quantity ], true)"
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
    //     "isSchemaLess()",
    //     "createDDLStatement()",
    //     "addSimpleProperty([ id ], true)",
    //     "addSimpleProperty([ number ], true)"
    // ]

}