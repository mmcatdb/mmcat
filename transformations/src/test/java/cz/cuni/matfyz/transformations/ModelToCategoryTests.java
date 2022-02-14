package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.schema.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cz.cuni.matfyz.core.utils.Debug;

/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategoryTests
{
    @BeforeEach
    public void setUp()
    {
        UniqueIdProvider.reset();
    }

	@Test
	public void basicTest()
    {
        var data = new TestData();

        SchemaCategory schema = new SchemaCategory();
        var order = data.buildOrder(schema);

        new ModelToCategoryTestBase("1BasicTest.json").setAll(
            schema,
            order,
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

        new ModelToCategoryTestBase("2StructureTest.json").setAll(
            schema,
            order,
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

        new ModelToCategoryTestBase("3SimpleArrayTest.json").setAll(
            schema,
            order,
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

        new ModelToCategoryTestBase("4ComplexArrayTest.json").setAll(
            schema,
            order,
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

        new ModelToCategoryTestBase("5MapTest.json").setAll(
            schema,
            order,
            data.path_contact(),
            data.expectedInstance_contact(schema)
        )
        .testAlgorithm();
	}

    @Test
	public void syntheticPropertyTest()
    {
        var data = new TestData();

        SchemaCategory schema = new SchemaCategory();
        var order = data.buildOrder(schema);
        data.addOrdered(schema, order);

        new ModelToCategoryTestBase("6SyntheticPropertyTest.json").setAll(
            schema,
            order,
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

        new ModelToCategoryTestBase("7MissingSimpleTest.json").setAll(
            schema,
            order,
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

        new ModelToCategoryTestBase("8MissingComplexTest.json").setAll(
            schema,
            order,
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

        new ModelToCategoryTestBase("9EmptyArrayTest.json").setAll(
            schema,
            order,
            data.path_items(),
            data.expectedInstance_itemsEmpty(schema)
        )
        .testAlgorithm();
	}

    @Test
	public void complexMapTest()
    {
        Debug.setLevel(3);

        var data = new TestData();

        SchemaCategory schema = new SchemaCategory();
        var order = data.buildOrder(schema);
        data.addAddress(schema, order);

        new ModelToCategoryTestBase("10ComplexMapTest.json").setAll(
            schema,
            order,
            data.path_address(),
            data.expectedInstance_address(schema)
        )
        .testAlgorithm();
	}

    @Test
	public void missingArrayTest()
    {
        Debug.setLevel(0);
        
        var data = new TestData();

        SchemaCategory schema = new SchemaCategory();
        var order = data.buildOrder(schema);
        data.addItems(schema, order);

        new ModelToCategoryTestBase("11MissingArrayTest.json").setAll(
            schema,
            order,
            data.path_items(),
            data.expectedInstance_itemsMissing(schema)
        )
        .testAlgorithm();
	}
}
