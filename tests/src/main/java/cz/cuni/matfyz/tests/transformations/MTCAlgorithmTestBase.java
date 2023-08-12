package cz.cuni.matfyz.tests.transformations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.cuni.matfyz.abstractwrappers.utils.PullQuery;
import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceCategoryBuilder;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.tests.instance.InstanceBuilder;
import cz.cuni.matfyz.tests.instance.InstanceBuilder.InstanceAdder;
import cz.cuni.matfyz.tests.mapping.TestMapping;
import cz.cuni.matfyz.transformations.algorithms.MTCAlgorithm;
import cz.cuni.matfyz.wrapperdummy.DummyPullWrapper;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;

/**
 * @author jachymb.bartik
 */
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
        InstanceCategory instance = new InstanceCategoryBuilder().setSchemaCategory(schema).build();

        for (final MappingWithRecords kind : kinds) {
            ForestOfRecords forest;
            try {
                forest = new DummyPullWrapper().pullForest(kind.mapping.accessPath(), PullQuery.fromString(kind.records));
            }
            catch (Exception e) {
                Assertions.fail(e.getMessage());
                return;
            }

            final var transformation = new MTCAlgorithm();
            transformation.input(kind.mapping, instance, forest);
            transformation.algorithm();
        }

        final var builder = new InstanceBuilder(schema);
        instanceAdder.add(builder);
        InstanceCategory expectedInstance = builder.build();

        assertEquals(expectedInstance.objects(), instance.objects(), "Test objects differ from the expected objects.");
        assertEquals(expectedInstance.morphisms(), instance.morphisms(), "Test morphisms differ from the expected morphisms.");

        for (var entry : expectedInstance.objects().entrySet()) {
            var expectedObject = entry.getValue();
            var object = instance.getObject(entry.getKey());

            var expectedString = expectedObject.allRowsToSet().stream().map(row -> rowToMappingsString(row, expectedObject)).toList();
            var string = object.allRowsToSet().stream().map(row -> rowToMappingsString(row, object)).toList();

            assertEquals(expectedString, string);
        }
    }

    private static String rowToMappingsString(DomainRow row, InstanceObject object) {
        String output = "\n[row] (" + object.key() + ") "  + row;

        for (var mappingsOfType : row.getAllMappingsFrom()) {
            output += "\n\tmappings [" + mappingsOfType.getKey().signature() + "]->(" + mappingsOfType.getKey().cod().key() + "):";
            for (var m : mappingsOfType.getValue())
                output += "\n\t\t" + m.codomainRow();
        }

        return output + "\n";
    }
}
