package cz.matfyz.tests.transformations;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.mapping.MongoDB;
import cz.matfyz.tests.mapping.PostgreSQL;
import cz.matfyz.tests.schema.BasicSchema;

import org.junit.jupiter.api.Test;

/**
 * @author jachymb.bartik
 */
public class DDLAlgorithmTests {

    private static final SchemaCategory schema = BasicSchema.newSchemaCategory();

    @Test
    public void basicTest() {
        new DDLAlgorithmTestBase(PostgreSQL.order(schema))
            .instance((builder) -> {
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
    public void structureTest() {
        new DDLAlgorithmTestBase(MongoDB.address(schema))
            .instance((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addAddress(builder, 0, "0", "hodnotaA", "hodnotaB", "hodnotaC");
                MongoDB.addAddress(builder, 1, "1", "hodnotaA2", "hodnotaB2", "hodnotaC2");
            })
            .expected("""
                [
                    "setKindName(order)",
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
    public void simpleArrayTest() {
        new DDLAlgorithmTestBase(MongoDB.tag(schema))
            .instance((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addTag(builder, 0, new String[]{ "123", "456", "789" });
                MongoDB.addTag(builder, 1, new String[]{ "123", "String456", "String789" });
            })
            .expected("""
                [
                    "setKindName(order)",
                    "isSchemaLess()",
                    "createDDLStatement()",
                    "addSimpleProperty([ number ], true)",
                    "addSimpleArrayProperty([ tags ], false)"
                ]
            """)
            .run();
    }

    @Test
    public void complexArrayTest() {
        new DDLAlgorithmTestBase(MongoDB.item(schema))
            .instance((builder) -> {
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
    public void mapTest() {
        new DDLAlgorithmTestBase(MongoDB.contact(schema))
            .instance((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addContact(builder, 0, "email", "anna@seznam.cz");
                MongoDB.addContact(builder, 0, "cellphone", "+420777123456");
                MongoDB.addContact(builder, 1, "skype", "skype123");
                MongoDB.addContact(builder, 1, "cellphone", "+420123456789");
            })
            .expected("""
                [
                    "setKindName(order)",
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
    public void syntheticPropertyTest() {
        new DDLAlgorithmTestBase(MongoDB.customer(schema))
            .instance((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addCustomer(builder, 0, "1");
                MongoDB.addCustomer(builder, 1, "1");
            })
            .expected("""
                [
                    "setKindName(order)",
                    "isSchemaLess()",
                    "createDDLStatement()",
                    "addSimpleProperty([ customer/number ], true)",
                    "addSimpleProperty([ customer/name ], true)"
                ]
            """)
            .run();
    }

    @Test
    public void missingSimpleTest() {
        new DDLAlgorithmTestBase(MongoDB.address(schema))
            .instance((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addAddress(builder, 0, "0", "hodnotaA", null, "hodnotaC");
                MongoDB.addAddress(builder, 1, "1", "hodnotaA2", null, "hodnotaC2");
            })
            .expected("""
                [
                    "setKindName(order)",
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
    public void missingComplexTest() {
        new DDLAlgorithmTestBase(MongoDB.address(schema))
            .instance((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
            })
            .expected("""
                [
                    "setKindName(order)",
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
    public void emptyArrayTest() {
        new DDLAlgorithmTestBase(MongoDB.item(schema))
            .instance((builder) -> {
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
    public void complexMapTest() {
        new DDLAlgorithmTestBase(MongoDB.note(schema))
            .instance((builder) -> {
                PostgreSQL.addOrder(builder, "o_100");
                PostgreSQL.addOrder(builder, "o_200");
                MongoDB.addNote(builder, 0, "cs-CZ", "0", "subject 1", "content cz");
                MongoDB.addNote(builder, 0, "en-US", "1", "subject 1", "content en");
                MongoDB.addNote(builder, 1, "cs-CZ", "3", "subject cz", "content 1");
                MongoDB.addNote(builder, 1, "en-GB", "2", "subject gb", "content 2");
            })
            .expected("""
                [
                    "setKindName(order)",
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
    public void missingArrayTest() {
        new DDLAlgorithmTestBase(MongoDB.item(schema))
            .instance((builder) -> {
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
    // public void selfIdentifierTest() {
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