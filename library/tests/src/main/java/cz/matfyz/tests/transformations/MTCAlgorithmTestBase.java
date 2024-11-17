package cz.matfyz.tests.transformations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceObject;
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

public class MTCAlgorithmTestBase {

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
        final InstanceCategory instance = new InstanceBuilder(schema).build();

        for (final MappingWithRecords kind : kinds) {
            ForestOfRecords forest;
            try {
                forest = new DummyPullWrapper().pullForest(kind.mapping.accessPath(), new StringQuery(kind.records));
            }
            catch (Exception e) {
                Assertions.fail(e.getMessage());
                return;
            }

            final var tform = new MTCAlgorithm();
            tform.input(kind.mapping, instance, forest);
            tform.algorithm();
        }

        final var builder = new InstanceBuilder(schema);
        instanceAdder.add(builder);
        InstanceCategory expectedInstance = builder.build();

        assertEquals(expectedInstance.allObjects(), instance.allObjects(), "Test objects differ from the expected objects.");
        assertEquals(expectedInstance.allMorphisms(), instance.allMorphisms(), "Test morphisms differ from the expected morphisms.");

        for (final var expectedObject : expectedInstance.allObjects()) {
            final var object = instance.getObject(expectedObject.schema.key());

            final var expectedString = expectedObject.allRowsToSet().stream().map(row -> rowToMappingsString(row, expectedObject)).toList();
            final var string = object.allRowsToSet().stream().map(row -> rowToMappingsString(row, object)).toList();

            assertEquals(expectedString, string);
        }
    }

    private static String rowToMappingsString(DomainRow row, InstanceObject object) {
        String output = "\n[row] (" + object.schema.key() + ") "  + row;

        for (final var mappingsOfType : row.getAllMappingsFrom()) {
            output += "\n\tmappings [" + mappingsOfType.morphism().schema.signature() + "]->(" + mappingsOfType.morphism().schema.cod().key() + "):";
            for (final var m : mappingsOfType.mappings())
                output += "\n\t\t" + m.codomainRow();
        }

        return output + "\n";
    }

}
