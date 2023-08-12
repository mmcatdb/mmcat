package cz.matfyz.tests.transformations;

import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.tests.instance.InstanceBuilder;
import cz.matfyz.tests.instance.InstanceBuilder.InstanceAdder;
import cz.matfyz.tests.mapping.TestMapping;
import cz.matfyz.transformations.algorithms.DDLAlgorithm;
import cz.matfyz.wrapperdummy.DummyDDLWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;


/**
 * @author jachymb.bartik
 */
public class DDLAlgorithmTestBase {

    private final Mapping mapping;

    public DDLAlgorithmTestBase(TestMapping testMapping) {
        this.mapping = testMapping.mapping();
    }

    private String expected;

    public DDLAlgorithmTestBase expected(String expected) {
        this.expected = expected;

        return this;
    }

    private InstanceCategory inputInstance;

    public DDLAlgorithmTestBase instance(InstanceAdder adder) {
        final var builder = new InstanceBuilder(mapping.category());
        adder.add(builder);
        inputInstance = builder.build();

        return this;
    }

    private List<String> buildExpectedResult() throws Exception {
        var json = new JSONArray(expected);
        var lines = new ArrayList<String>();
        
        for (int i = 0; i < json.length(); i++)
            lines.add(json.getString(i));
        
        return lines;
    }

    public void run() {
        List<String> expectedResult;
        try {
            expectedResult = buildExpectedResult();
        }
        catch (Exception e) {
            Assertions.fail("Exception thrown when loading test data.", e);
            return;
        }

        var wrapper = new DummyDDLWrapper();
        var transformation = new DDLAlgorithm();
        transformation.input(mapping, inputInstance, wrapper);
        transformation.algorithm();

        List<String> result = wrapper.methods();

        printResult(result);

        Assertions.assertTrue(resultsEquals(expectedResult, result), "Test objects differ from the expected objects.");
    }

    private static void printResult(List<String> result) {
        var builder = new StringBuilder();

        builder.append("[\n");
        result.forEach(line -> builder.append("    ").append(line).append("\n"));
        builder.append("]");

        System.out.println(builder.toString());
    }

    private static boolean resultsEquals(List<String> result1, List<String> result2) {
        if (result1.size() != result2.size())
            return false;

        var set1 = new TreeSet<>(result1);
        var set2 = new TreeSet<>(result2);

        return set1.equals(set2);
    }
}
