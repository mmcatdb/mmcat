package cz.matfyz.tests.querying;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import cz.matfyz.core.querying.QueryStructure;
import cz.matfyz.core.querying.queryresult.*;
import cz.matfyz.querying.algorithms.queryresult.*;
import cz.matfyz.querying.algorithms.queryresult.TformStep.TformRoot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class ProjectionTestBase {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectionTestBase.class);

    private QueryStructure input;

    public ProjectionTestBase input(QueryStructure input) {
        this.input = input;
        return this;
    }

    private TformingQueryStructure output;

    public ProjectionTestBase output(TformingQueryStructure output) {
        this.output = output;
        return this;
    }

    private String expectedTform;

    public ProjectionTestBase expectedTform(String expectedTform) {
        this.expectedTform = expectedTform;
        return this;
    }

    private String dataRaw;

    public ProjectionTestBase data(String dataRaw) {
        this.dataRaw = dataRaw;
        return this;
    }

    private String expectedDataRaw;

    public ProjectionTestBase expectedData(String expectedDataRaw) {
        this.expectedDataRaw = expectedDataRaw;
        return this;
    }

    private ResultList data;
    private ResultList expectedData;

    public void run() {
        final TformRoot actualTform = QueryStructureTformer.run(input, output);
        assertEquals(expectedTform, actualTform.toString());

        assertDoesNotThrow(() -> data = (ResultList) ResultNode.JsonBuilder.fromJson(dataRaw));
        assertDoesNotThrow(() -> expectedData = (ResultList) ResultNode.JsonBuilder.fromJson(expectedDataRaw));

        final var context = new TformContext(data);
        actualTform.apply(context);
        final ResultList actualData = (ResultList) context.getOutput();
        compareNode(expectedData, actualData);
    }

    private void compareNode(ResultNode expected, ResultNode actual) {
        if (expected instanceof ResultList expectedList && actual instanceof ResultList actualList) {
            compareList(expectedList, actualList);
            return;
        }

        if (expected instanceof ResultMap expectedMap && actual instanceof ResultMap actualMap) {
            compareMap(expectedMap, actualMap);
            return;
        }

        if (expected instanceof ResultLeaf expectedLeaf && actual instanceof ResultLeaf actualLeaf) {
            compareLeaf(expectedLeaf, actualLeaf);
            return;
        }

        fail("Cannot compare " + expected.getClass() + " and " + actual.getClass());
    }

    private void compareList(ResultList expected, ResultList actual) {
        assertEquals(expected.children().size(), actual.children().size());

        for (int i = 0; i < expected.children().size(); i++)
            compareNode(expected.children().get(i), actual.children().get(i));
    }

    private void compareMap(ResultMap expected, ResultMap actual) {
        assertEquals(expected.children().size(), actual.children().size());

        for (final var entry : expected.children().entrySet())
            compareNode(entry.getValue(), actual.children().get(entry.getKey()));
    }

    private void compareLeaf(ResultLeaf expected, ResultLeaf actual) {
        assertEquals(expected.value, actual.value);
    }
}
