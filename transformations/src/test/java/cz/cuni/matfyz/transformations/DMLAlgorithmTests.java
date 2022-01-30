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
public class DMLAlgorithmTests
{
	@Test
	public void basicTest()
    {
        var data = new TestData();

        SchemaCategory schema = new SchemaCategory();
        var order = data.buildOrder(schema);

        new DMLAlgorithmTestBase("1BasicTest.json").setAll(
            schema,
            order,
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

        new DMLAlgorithmTestBase("2StructureTest.json").setAll(
            schema,
            order,
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

        new DMLAlgorithmTestBase("3SimpleArrayTest.json").setAll(
            schema,
            order,
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

        new DMLAlgorithmTestBase("4ComplexArrayTest.json").setAll(
            schema,
            order,
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

        new DMLAlgorithmTestBase("5MapTest.json").setAll(
            schema,
            order,
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

        new DMLAlgorithmTestBase("6SyntheticPropertyTest.json").setAll(
            schema,
            order,
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

        new DMLAlgorithmTestBase("7MissingSimpleTest.json").setAll(
            schema,
            order,
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

        new DMLAlgorithmTestBase("8MissingComplexTest.json").setAll(
            schema,
            order,
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

        new DMLAlgorithmTestBase("9EmptyArrayTest.json").setAll(
            schema,
            order,
            "order",
            data.path_items(),
            data.expectedInstance_itemsMissing(schema)
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

        new DMLAlgorithmTestBase("10ComplexMapTest.json").setAll(
            schema,
            order,
            "order",
            data.path_address(),
            data.expectedInstance_address(schema)
        )
        .testAlgorithm();
	}
}