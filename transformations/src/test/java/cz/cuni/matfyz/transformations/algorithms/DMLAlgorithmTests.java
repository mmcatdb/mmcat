package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class DMLAlgorithmTests
{
    private TestData data;
    private SchemaCategory schema;
    private SchemaObject order;

    @BeforeEach
    public void setupTestData()
    {
        data = new TestData();
        schema = data.createDefaultSchemaCategory();
        order = schema.keyToObject(data.getOrderKey());
    }
    
	@Test
	public void basicTest()
    {
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
        //Debug.setLevel(0);

        new DMLAlgorithmTestBase("3SimpleArrayTest.json").setAll(
            schema,
            order,
            "order",
            data.path_array(),
            data.expectedInstance_array(schema)
        )
        .testAlgorithm();
	}

    // This isn't a comprehensive test because it doesn't check if the order is correct. However, to implement a complete test would be such an overkill.
    @Test
	public void complexArrayTest()
    {
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
        //Debug.setLevel(0);

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
        new DMLAlgorithmTestBase("9EmptyArrayTest.json").setAll(
            schema,
            order,
            "order",
            data.path_items(),
            data.expectedInstance_itemsEmpty(schema)
        )
        .testAlgorithm();
	}

    @Test
	public void complexMapTest()
    {
        new DMLAlgorithmTestBase("10ComplexMapTest.json").setAll(
            schema,
            order,
            "order",
            data.path_address(),
            data.expectedInstance_address(schema)
        )
        .testAlgorithm();
	}

    @Test
	public void missingArrayTest()
    {
        new DMLAlgorithmTestBase("11MissingArrayTest.json").setAll(
            schema,
            order,
            "order",
            data.path_items(),
            data.expectedInstance_itemsMissing(schema)
        )
        .testAlgorithm();
	}
}
