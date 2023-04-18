package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.tests.TestData;
import cz.cuni.matfyz.core.utils.UniqueIdProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author jachymb.bartik
 */
public class MTCAlgorithmTests {

    private TestData data;
    private SchemaCategory schema;
    private SchemaObject order;


    @BeforeEach
    public void setUp() {
        UniqueIdProvider.reset();
        data = new TestData();
        schema = data.createDefaultSchemaCategory();
        order = schema.getObject(data.orderKey);
    }

    @Test
    public void basicTest() {
        new MTCAlgorithmTestBase("1BasicTest.json").setAll(
            schema,
            order,
            data.path_orderRoot(),
            data.expectedInstance_order(schema)
        )
            .testAlgorithm();
    }

    @Test
    public void structureTest() {
        new MTCAlgorithmTestBase("2StructureTest.json").setAll(
            schema,
            order,
            data.path_nestedDoc(),
            data.expectedInstance_nestedDoc(schema)
        )
            .testAlgorithm();
    }

    @Test
    public void simpleArrayTest() {
        new MTCAlgorithmTestBase("3SimpleArrayTest.json").setAll(
            schema,
            order,
            data.path_array(),
            data.expectedInstance_array(schema)
        )
            .testAlgorithm();
    }

    @Test
    public void complexArrayTest() {
        new MTCAlgorithmTestBase("4ComplexArrayTest.json").setAll(
            schema,
            order,
            data.path_items(),
            data.expectedInstance_items(schema)
        )
            .testAlgorithm();
    }

    @Test
    public void mapTest() {
        new MTCAlgorithmTestBase("5MapTest.json").setAll(
            schema,
            order,
            data.path_contact(),
            data.expectedInstance_contact(schema)
        )
            .testAlgorithm();
    }

    @Test
    public void syntheticPropertyTest() {
        new MTCAlgorithmTestBase("6SyntheticPropertyTest.json").setAll(
            schema,
            order,
            data.path_ordered(),
            data.expectedInstance_ordered(schema)
        )
            .testAlgorithm();
    }

    @Test
    public void missingSimpleTest() {
        new MTCAlgorithmTestBase("7MissingSimpleTest.json").setAll(
            schema,
            order,
            data.path_nestedDoc(),
            data.expectedInstance_nestedDocMissingSimple(schema)
        )
            .testAlgorithm();
    }

    @Test
    public void missingComplexTest() {
        new MTCAlgorithmTestBase("8MissingComplexTest.json").setAll(
            schema,
            order,
            data.path_nestedDoc(),
            data.expectedInstance_nestedDocMissingComplex(schema)
        )
            .testAlgorithm();
    }

    @Test
    public void emptyArrayTest() {
        new MTCAlgorithmTestBase("9EmptyArrayTest.json").setAll(
            schema,
            order,
            data.path_items(),
            data.expectedInstance_itemsEmpty(schema)
        )
            .testAlgorithm();
    }

    @Test
    public void complexMapTest() {
        new MTCAlgorithmTestBase("10ComplexMapTest.json").setAll(
            schema,
            order,
            data.path_address(),
            data.expectedInstance_address(schema)
        )
            .testAlgorithm();
    }

    @Test
    public void missingArrayTest() {
        new MTCAlgorithmTestBase("11MissingArrayTest.json").setAll(
            schema,
            order,
            data.path_items(),
            data.expectedInstance_itemsMissing(schema)
        )
            .testAlgorithm();
    }

    @Test
    public void selfIdentifierTest() {
        final var schemaV3 = data.createDefaultV3SchemaCategory();

        new MTCAlgorithmTestBase("12SelfIdentifierTest.json").setAll(
            schemaV3,
            schemaV3.getObject(data.orderKey),
            data.path_orderV3Root(),
            data.expectedInstance_selfIdentifier(schemaV3)
        )
            .testAlgorithm();
    }
}
