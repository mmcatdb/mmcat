package cz.matfyz.tests.transformations;

import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceBuilder.InstanceAdder;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.tests.example.common.TestMapping;
import cz.matfyz.transformations.algorithms.DMLAlgorithm;
import cz.matfyz.wrapperdummy.DMLTestStructure;
import cz.matfyz.wrapperdummy.DummyDMLWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMLAlgorithmTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DMLAlgorithmTestBase.class);

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
        final List<DMLTestStructure> expectedResult;
        try {
            expectedResult = buildExpectedResult();
        }
        catch (Exception e) {
            Assertions.fail("Exception thrown when loading test data.");
            return;
        }

        final var wrapper = new DummyDMLWrapper();
        DMLAlgorithm.run(mapping, inputInstance, wrapper);

        final List<DMLTestStructure> actualResult = wrapper.structures();

        final var actualString = resultToString(actualResult);
        LOGGER.debug("ACTUAL:\n{}", actualString);
        final var expectedString = resultToString(expectedResult);
        LOGGER.debug("EXPECTED:\n{}", expectedString);

        assertEquals(expectedString, actualString);
    }

    private static String resultToString(List<DMLTestStructure> result) {
        return result.stream()
            .map(DMLTestStructure::toString)
            .sorted()
            .collect(Collectors.joining(",\n"));
    }

}
