package cz.matfyz.tests.transformations;

import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceBuilder.InstanceAdder;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.tests.example.common.TestMapping;
import cz.matfyz.transformations.algorithms.DDLAlgorithm;
import cz.matfyz.wrapperdummy.DummyDDLWrapper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DDLAlgorithmTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DDLAlgorithmTestBase.class);

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
        final var json = new JSONArray(expected);
        final var lines = new ArrayList<String>();

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

        final var wrapper = new DummyDDLWrapper();
        DDLAlgorithm.run(mapping, inputInstance, wrapper);

        final List<String> actualResult = wrapper.methods();

        final var actualString = resultToString(actualResult);
        LOGGER.debug("ACTUAL:\n{}", actualString);
        final var expectedString = resultToString(expectedResult);
        LOGGER.debug("EXPECTED:\n{}", expectedString);

        assertEquals(expectedString, actualString);
    }

    private static String resultToString(List<String> result) {
        final var builder = new StringBuilder();

        builder.append("[\n");
        result.stream().sorted().forEach(line -> builder.append("    ").append(line).append(",\n"));
        builder.append("]");

        return builder.toString();
    }

}
