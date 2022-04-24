package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.schema.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategoryTests
{
    private TestData data;
    private SchemaCategory schema;
    private SchemaObject order;


    @BeforeEach
    public void setUp()
    {
        UniqueIdProvider.reset();
        data = new TestData();
        schema = data.createDefaultSchemaCategory();
        order = schema.keyToObject(data.getOrderKey());
    }

	@Test
	public void basicTest()
    {
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
        //Debug.setLevel(3);

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
        //Debug.setLevel(0);
        
        new ModelToCategoryTestBase("11MissingArrayTest.json").setAll(
            schema,
            order,
            data.path_items(),
            data.expectedInstance_itemsMissing(schema)
        )
        .testAlgorithm();
	}
}
