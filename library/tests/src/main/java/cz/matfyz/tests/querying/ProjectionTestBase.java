package cz.matfyz.tests.querying;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import cz.matfyz.core.querying.LeafResult;
import cz.matfyz.core.querying.ListResult;
import cz.matfyz.core.querying.MapResult;
import cz.matfyz.core.querying.ResultNode;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.querying.resolver.queryresult.*;
import cz.matfyz.querying.resolver.queryresult.TformStep.TformRoot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectionTestBase {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectionTestBase.class);

    private ResultStructure input;

    public ProjectionTestBase input(ResultStructure input) {
        this.input = input;
        return this;
    }

    private TformingResultStructure output;

    public ProjectionTestBase output(TformingResultStructure output) {
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

    private ListResult data;
    private ListResult expectedData;

    public void run() {
        final TformRoot actualTform = ResultStructureTformer.run(input, output);
        assertEquals(expectedTform, actualTform.toString());

        assertDoesNotThrow(() -> data = (ListResult) ResultNode.JsonBuilder.fromJson(dataRaw));
        assertDoesNotThrow(() -> expectedData = (ListResult) ResultNode.JsonBuilder.fromJson(expectedDataRaw));

        final var context = new TformContext(data);
        actualTform.apply(context);
        final ListResult actualData = (ListResult) context.getOutput();
        compareNode(expectedData, actualData);
    }

    private void compareNode(ResultNode expected, ResultNode actual) {
        if (expected instanceof ListResult expectedList && actual instanceof ListResult actualList) {
            compareList(expectedList, actualList);
            return;
        }

        if (expected instanceof MapResult expectedMap && actual instanceof MapResult actualMap) {
            compareMap(expectedMap, actualMap);
            return;
        }

        if (expected instanceof LeafResult expectedLeaf && actual instanceof LeafResult actualLeaf) {
            compareLeaf(expectedLeaf, actualLeaf);
            return;
        }

        fail("Cannot compare " + expected.getClass() + " and " + actual.getClass());
    }

    private void compareList(ListResult expected, ListResult actual) {
        assertEquals(expected.children().size(), actual.children().size());

        for (int i = 0; i < expected.children().size(); i++)
            compareNode(expected.children().get(i), actual.children().get(i));
    }

    private void compareMap(MapResult expected, MapResult actual) {
        assertEquals(expected.children().size(), actual.children().size());

        for (final var entry : expected.children().entrySet())
            compareNode(entry.getValue(), actual.children().get(entry.getKey()));
    }

    private void compareLeaf(LeafResult expected, LeafResult actual) {
        assertEquals(expected.value, actual.value);
    }
}
