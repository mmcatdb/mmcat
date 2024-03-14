package cz.matfyz.tests.transformations;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.tests.example.common.TestMapping;
import cz.matfyz.transformations.algorithms.ICAlgorithm;
import cz.matfyz.wrapperdummy.DummyICWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;

/**
 * @author jachymb.bartik
 */
public class ICAlgorithmTestBase {

    private Mapping mapping;

    public ICAlgorithmTestBase primaryMapping(TestMapping testMapping) {
        mapping = testMapping.mapping();

        return this;
    }

    private final Set<Mapping> otherMappings = new TreeSet<>();

    public ICAlgorithmTestBase otherMappings(TestMapping... testMappings) {
        otherMappings.addAll(Stream.of(testMappings).map(testMapping -> testMapping.mapping()).toList());

        return this;
    }

    private String expected;

    public ICAlgorithmTestBase expected(String expected) {
        this.expected = expected;

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
        final List<String> expectedResult;
        try {
            expectedResult = buildExpectedResult();
        }
        catch (Exception e) {
            Assertions.fail("Exception thrown when loading test data.");
            return;
        }

        final var wrapper = new DummyICWrapper();

        final var tform = new ICAlgorithm();
        otherMappings.add(mapping);
        tform.input(mapping, otherMappings, wrapper);
        tform.algorithm();

        final List<String> result = wrapper.methods();

        System.out.println("\n[Expected]:");
        System.out.println(expectedResult);
        System.out.println("\n[Actual]:");
        System.out.println(result);

        Assertions.assertTrue(resultsEquals(expectedResult, result), "Test objects differ from the expected objects.");
    }

    private static boolean resultsEquals(List<String> result1, List<String> result2) {
        if (result1.size() != result2.size())
            return false;

        var set1 = new TreeSet<>(result1);
        var set2 = new TreeSet<>(result2);

        return set1.equals(set2);
    }

}
