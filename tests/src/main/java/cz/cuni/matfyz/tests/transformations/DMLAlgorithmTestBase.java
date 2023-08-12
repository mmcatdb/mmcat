package cz.cuni.matfyz.tests.transformations;

import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.tests.instance.InstanceBuilder;
import cz.cuni.matfyz.tests.instance.InstanceBuilder.InstanceAdder;
import cz.cuni.matfyz.tests.mapping.TestMapping;
import cz.cuni.matfyz.transformations.algorithms.DMLAlgorithm;
import cz.cuni.matfyz.wrapperdummy.DMLTestStructure;
import cz.cuni.matfyz.wrapperdummy.DummyDMLWrapper;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;

/**
 * @author jachymb.bartik
 */
public class DMLAlgorithmTestBase {

    private final Mapping mapping;

    public DMLAlgorithmTestBase(TestMapping testMapping) {
        this.mapping = testMapping.mapping();
    }

    private String expected;

    public DMLAlgorithmTestBase expected(String expected) {
        this.expected = expected;

        return this;
    }

    private InstanceCategory inputInstance;

    public DMLAlgorithmTestBase instance(InstanceAdder adder) {
        final var builder = new InstanceBuilder(mapping.category());
        adder.add(builder);
        inputInstance = builder.build();

        return this;
    }

    private List<DMLTestStructure> buildExpectedResult() throws Exception {
        var json = new JSONArray(expected);
        var structures = new ArrayList<DMLTestStructure>();
        
        for (int i = 0; i < json.length(); i++)
            structures.add(new DMLTestStructure(json.getJSONObject(i)));
        
        return structures;
    }

    public void run() {
        List<DMLTestStructure> expectedResult;
        try {
            expectedResult = buildExpectedResult();
        }
        catch (Exception e) {
            Assertions.fail("Exception thrown when loading test data.");
            return;
        }

        var wrapper = new DummyDMLWrapper();
        var transformation = new DMLAlgorithm();
        transformation.input(mapping, inputInstance, wrapper);
        transformation.algorithm();

        List<DMLTestStructure> result = wrapper.structures();

        System.out.println(result);
        Assertions.assertTrue(resultsEquals(expectedResult, result), "Test objects differ from the expected objects.");
    }

    private static boolean resultsEquals(List<DMLTestStructure> result1, List<DMLTestStructure> result2) {
        if (result1.size() != result2.size())
            return false;

        for (var structure1 : result1) {
            boolean equals = false;
            for (var structure2 : result2)
                if (structure1.equals(structure2)) {
                    equals = true;
                    break;
                }

            if (!equals)
                return false;
        }

        return true;
    }
}
