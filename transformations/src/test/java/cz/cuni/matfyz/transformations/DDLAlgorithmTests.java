package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.utils.Debug;

import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class DDLAlgorithmTests
{
	@Test
	public void basicTest()
    {
        var data = new TestData();

        SchemaCategory schema = new SchemaCategory();
        data.buildOrder(schema);

        new DDLAlgorithmTestBase("1BasicTest.json").setAll(
            schema,
            "order",
            data.path_order(),
            data.expectedInstance_order(schema)
        )
        .testAlgorithm();
	}

    @Test
	public void structureTest()
    {
        var data = new TestData();

        SchemaCategory schema = new SchemaCategory();
        var order = data.buildOrder(schema);
        data.addNestedDoc(schema, order);

        new DDLAlgorithmTestBase("2StructureTest.json").setAll(
            schema,
            "order",
            data.path_nestedDoc(),
            data.expectedInstance_nestedDoc(schema)
        )
        .testAlgorithm();
	}

    @Test
	public void simpleArrayTest()
    {
        var data = new TestData();
        Debug.setLevel(0);

        SchemaCategory schema = new SchemaCategory();
        var order = data.buildOrder(schema);
        data.addArray(schema, order);

        new DDLAlgorithmTestBase("3SimpleArrayTest.json").setAll(
            schema,
            "order",
            data.path_array(),
            data.expectedInstance_array(schema)
        )
        .testAlgorithm();
	}

    @Test
	public void complexArrayTest()
    {
        var data = new TestData();

        SchemaCategory schema = new SchemaCategory();
        var order = data.buildOrder(schema);
        data.addItems(schema, order);

        new DDLAlgorithmTestBase("4ComplexArrayTest.json").setAll(
            schema,
            "order",
            data.path_items(),
            data.expectedInstance_items(schema)
        )
        .testAlgorithm();
	}

    @Test
	public void mapTest()
    {
        var data = new TestData();

        SchemaCategory schema = new SchemaCategory();
        var order = data.buildOrder(schema);
        data.addContact(schema, order);

        new DDLAlgorithmTestBase("5MapTest.json").setAll(
            schema,
            "order",
            data.path_contact(),
            data.expectedInstance_contact(schema)
        )
        .testAlgorithm();
	}

    @Test
	public void syntheticPropertyTest()
    {
        Debug.setLevel(0);
        
        var data = new TestData();

        SchemaCategory schema = new SchemaCategory();
        var order = data.buildOrder(schema);
        data.addOrdered(schema, order);

        new DDLAlgorithmTestBase("6SyntheticPropertyTest.json").setAll(
            schema,
            "order",
            data.path_ordered(),
            data.expectedInstance_ordered(schema)
        )
        .testAlgorithm();
	}

    @Test
	public void missingSimpleTest()
    {
        var data = new TestData();

        SchemaCategory schema = new SchemaCategory();
        var order = data.buildOrder(schema);
        data.addNestedDoc(schema, order);

        new DDLAlgorithmTestBase("7MissingSimpleTest.json").setAll(
            schema,
            "order",
            data.path_nestedDoc(),
            data.expectedInstance_nestedDocMissingSimple(schema)
        )
        .testAlgorithm();
	}

    @Test
	public void missingComplexTest()
    {
        var data = new TestData();

        SchemaCategory schema = new SchemaCategory();
        var order = data.buildOrder(schema);
        data.addNestedDoc(schema, order);

        new DDLAlgorithmTestBase("8MissingComplexTest.json").setAll(
            schema,
            "order",
            data.path_nestedDoc(),
            data.expectedInstance_nestedDocMissingComplex(schema)
        )
        .testAlgorithm();
	}

    @Test
	public void emptyArrayTest()
    {
        var data = new TestData();

        SchemaCategory schema = new SchemaCategory();
        var order = data.buildOrder(schema);
        data.addItems(schema, order);

        new DDLAlgorithmTestBase("9EmptyArrayTest.json").setAll(
            schema,
            "order",
            data.path_items(),
            data.expectedInstance_itemsEmpty(schema)
        )
        .testAlgorithm();
	}

    @Test
	public void complexMapTest()
    {
        var data = new TestData();

        SchemaCategory schema = new SchemaCategory();
        var order = data.buildOrder(schema);
        data.addAddress(schema, order);

        new DDLAlgorithmTestBase("10ComplexMapTest.json").setAll(
            schema,
            "order",
            data.path_address(),
            data.expectedInstance_address(schema)
        )
        .testAlgorithm();
	}

    @Test
	public void missingArrayTest()
    {
        var data = new TestData();

        SchemaCategory schema = new SchemaCategory();
        var order = data.buildOrder(schema);
        data.addItems(schema, order);

        new DDLAlgorithmTestBase("11MissingArrayTest.json").setAll(
            schema,
            "order",
            data.path_items(),
            data.expectedInstance_itemsMissing(schema)
        )
        .testAlgorithm();
	}
}