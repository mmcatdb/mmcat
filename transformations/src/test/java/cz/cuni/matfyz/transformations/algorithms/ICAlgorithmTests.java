package cz.cuni.matfyz.transformations.algorithms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author jachymb.bartik
 */
public class ICAlgorithmTests {

    private TestData data;
    private ICAlgorithmTestBase testBase;

    @BeforeEach
    public void setupTestData() {
        data = new TestData();
        final var schema = data.createDefaultSchemaCategory();

        testBase = new ICAlgorithmTestBase(schema)
            .addMappingForTest(schema.getObject(data.orderKey), "order", data.path_orderRoot())
            .addMappingForTest(schema.getObject(data.contactKey), "contact", data.path_contactRoot());

        System.out.print("");
    }

    @Test
    public void basicPrimaryKeyTest() {
        testBase.testAlgorithm("1BasicPrimaryKey.json", "order");
    }

    @Test
    public void structureTest() {
        testBase.testAlgorithm("2ComplexPrimaryKey.json", "contact");
    }

    @Test
    public void simpleArrayTest() {
        testBase.testAlgorithm("3SimpleArrayTest.json", "order");
    }

    @Test
    public void complexArrayTest() {
        testBase.testAlgorithm("4ComplexArrayTest.json", "order");
    }

    @Test
    public void mapTest() {
        testBase.testAlgorithm("5MapTest.json", "order");
    }

    @Test
    public void syntheticPropertyTest() {
        testBase.testAlgorithm("6SyntheticPropertyTest.json", "order");
    }

    @Test
    public void complexMapTest() {
        testBase.testAlgorithm("10ComplexMapTest.json", "order");
    }

}