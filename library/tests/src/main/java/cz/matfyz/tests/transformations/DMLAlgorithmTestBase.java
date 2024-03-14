package cz.matfyz.tests.transformations;

import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.tests.example.common.InstanceBuilder;
import cz.matfyz.tests.example.common.TestMapping;
import cz.matfyz.tests.example.common.InstanceBuilder.InstanceAdder;
import cz.matfyz.transformations.algorithms.DMLAlgorithm;
import cz.matfyz.wrapperdummy.DMLTestStructure;
import cz.matfyz.wrapperdummy.DummyDMLWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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

    private String expectedString;

    public DMLAlgorithmTestBase expected(String expected) {
        this.expectedString = expected;

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
        var json = new JSONArray(expectedString);
        var structures = new ArrayList<DMLTestStructure>();

        for (int i = 0; i < json.length(); i++)
            structures.add(new DMLTestStructure(json.getJSONObject(i)));

        return structures;
    }

    public void run() {
        List<DMLTestStructure> expected;
        try {
            expected = buildExpectedResult();
        }
        catch (Exception e) {
            Assertions.fail("Exception thrown when loading test data.");
            return;
        }

        final var wrapper = new DummyDMLWrapper();
        final var tform = new DMLAlgorithm();
        tform.input(mapping, inputInstance, wrapper);
        tform.algorithm();

        final List<DMLTestStructure> actual = wrapper.structures();

        assertResults(expected, actual);
    }

    private static void assertResults(List<DMLTestStructure> expected, List<DMLTestStructure> actual) {
        Assertions.assertEquals(
            expected.size(),
            actual.size(),
            new FailMessage(expected, actual, "Sizes aren't equal.\nexpected: " + expected.size() + "\nactual: " + actual.size())
        );

        for (final DMLTestStructure expectedStructure : expected) {
            final boolean match = actual.stream().anyMatch(actualStructure -> actualStructure.equals(expectedStructure));
            if (!match) {
                Assertions.fail(new FailMessage(expected, actual,
                "Expected structure not found in the actual result. Structure:\n" + expectedStructure.toString()
                ));
            }
        }
    }

    private record FailMessage(
        List<DMLTestStructure> expected,
        List<DMLTestStructure> actual,
        String message
    ) implements Supplier<String> {

        private static final String SEPARATOR = "################";

        public String get() {
            return message
                + "\n\n" + SEPARATOR + " expected: " + SEPARATOR
                + "\n" + expected
                + "\n\n" + SEPARATOR + " actual: " + SEPARATOR
                + "\n" + actual
                + "\n";
        }

    }

}
