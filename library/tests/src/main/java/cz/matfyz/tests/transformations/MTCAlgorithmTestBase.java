package cz.matfyz.tests.transformations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceObjex;
import cz.matfyz.core.instance.InstanceBuilder.InstanceAdder;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;
import cz.matfyz.transformations.algorithms.MTCAlgorithm;
import cz.matfyz.wrapperdummy.DummyPullWrapper;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MTCAlgorithmTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(MTCAlgorithmTestBase.class);

    private record MappingWithRecords(
        Mapping mapping,
        String records
    ) {}

    private final List<MappingWithRecords> kinds = new ArrayList<>();

    public MTCAlgorithmTestBase mappingWithRecords(TestMapping testMapping, String records) {
        this.kinds.add(new MappingWithRecords(testMapping.mapping(), records));

        return this;
    }

    private InstanceAdder instanceAdder;

    public MTCAlgorithmTestBase expected(InstanceAdder instanceAdder) {
        this.instanceAdder = instanceAdder;

        return this;
    }

    public void run() {
        final SchemaCategory schema = kinds.get(0).mapping.category();
        final InstanceCategory actualInstance = new InstanceBuilder(schema).build();

        for (final MappingWithRecords kind : kinds) {
            ForestOfRecords forest;
            try {
                forest = new DummyPullWrapper().pullForest(kind.mapping.accessPath(), new StringQuery(kind.records));
            }
            catch (Exception e) {
                Assertions.fail(e.getMessage());
                return;
            }

            LOGGER.debug("FOREST ({}):\n{}", kind.mapping.toString(), forest);

            MTCAlgorithm.run(kind.mapping, actualInstance, forest);
        }

        final var builder = new InstanceBuilder(schema);
        instanceAdder.add(builder);
        final InstanceCategory expectedInstance = builder.build();

        LOGGER.debug("ACTUAL:\n{}", actualInstance);
        LOGGER.debug("EXPECTED:\n{}", expectedInstance);

        // The stream - toList shenanigans is here to ensure both collections are the same "type" of list and thus can be compared.
        assertEquals(expectedInstance.allObjexes().stream().toList(), actualInstance.allObjexes().stream().toList(), "Test objexes differ from the expected objexes.");
        assertEquals(expectedInstance.allMorphisms().stream().toList(), actualInstance.allMorphisms().stream().toList(), "Test morphisms differ from the expected morphisms.");

        for (final var expectedObjex : expectedInstance.allObjexes()) {
            final var objex = actualInstance.getObjex(expectedObjex.schema.key());

            final var expectedString = expectedObjex.allRowsToSet().stream()
                .map(row -> rowToMappingsString(row, expectedObjex, schema))
                .sorted().toList();
            final var string = objex.allRowsToSet().stream()
                .map(row -> rowToMappingsString(row, objex, schema))
                .sorted().toList();

            assertEquals(expectedString, string);
        }
    }

    private final static String GENERATED_ID = "<generated>";

    private static String rowToMappingsString(DomainRow row, InstanceObjex objex, SchemaCategory schema) {
        String output = "\n[row] (" + objex.schema.key() + ") "  + row.toStringWithoutGeneratedIds(GENERATED_ID);

        for (final var entry : row.getAllMappingsFrom()) {
            final var signature = entry.getKey();
            final var codObjex = schema.getMorphism(signature).cod();

            output += "\n\tmappings --[" + entry.getKey() + "]->(" + codObjex.key() + "): " + entry.getValue().cod().toStringWithoutGeneratedIds(GENERATED_ID);
        }

        return output + "\n";
    }

}
