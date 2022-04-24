package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.schema.SchemaCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class DDLAlgorithmTests
{
    private TestData data;
    private SchemaCategory schema;

    @BeforeEach
    public void setupTestData()
    {
        data = new TestData();
        schema = data.createDefaultSchemaCategory();
    }

	@Test
	public void basicTest()
    {
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
        //Debug.setLevel(0);

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
        //Debug.setLevel(0);
        
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
        new DDLAlgorithmTestBase("11MissingArrayTest.json").setAll(
            schema,
            "order",
            data.path_items(),
            data.expectedInstance_itemsMissing(schema)
        )
        .testAlgorithm();
	}
}