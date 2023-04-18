package cz.cuni.matfyz.transformations.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.cuni.matfyz.abstractwrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceCategoryBuilder;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.wrapperdummy.DummyPullWrapper;

import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;

/**
 * @author jachymb.bartik
 */
public class MTCAlgorithmTestBase {

    private final String fileNamePrefix = "modelToCategory/";
    private final String dataFileName;

    public MTCAlgorithmTestBase(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public MTCAlgorithmTestBase setAll(SchemaCategory schema, SchemaObject rootObject, ComplexProperty path, InstanceCategory expectedInstance) {
        return setSchema(schema).setRootObject(rootObject).setPath(path).setExpectedInstance(expectedInstance);
    }

    private SchemaCategory schema;

    public MTCAlgorithmTestBase setSchema(SchemaCategory schema) {
        this.schema = schema;
        
        return this;
    }

    private InstanceCategory expectedCategory;

    public MTCAlgorithmTestBase setExpectedInstance(InstanceCategory expectedInstance) {
        this.expectedCategory = expectedInstance;

        return this;
    }

    private SchemaObject rootObject;

    public MTCAlgorithmTestBase setRootObject(SchemaObject rootObject) {
        this.rootObject = rootObject;

        return this;
    }

    private ComplexProperty path;

    public MTCAlgorithmTestBase setPath(ComplexProperty path) {
        this.path = path;

        return this;
    }

    private ForestOfRecords buildForestOfRecords(ComplexProperty path) throws Exception {
        var wrapper = new DummyPullWrapper();
        
        var url = ClassLoader.getSystemResource(fileNamePrefix + dataFileName);
        String fileName = Paths.get(url.toURI()).toAbsolutePath().toString();
        
        return wrapper.pullForest(path, new PullWrapperOptions.Builder().buildWithKindName(fileName));
    }

    public void testAlgorithm() {            
        InstanceCategory category = new InstanceCategoryBuilder().setSchemaCategory(schema).build();

        ForestOfRecords forest;
        try {
            forest = buildForestOfRecords(path);
        }
        catch (Exception e) {
            Assertions.fail(e.getMessage());
            return;
        }

        Mapping mapping = new Mapping(schema, rootObject, path, null, null);

        var transformation = new MTCAlgorithm();
        transformation.input(mapping, category, forest);
        transformation.algorithm();

        Assertions.assertEquals(expectedCategory.objects(), category.objects(), "Test objects differ from the expected objects.");
        Assertions.assertEquals(expectedCategory.morphisms(), category.morphisms(), "Test morphisms differ from the expected morphisms.");

        for (var entry : expectedCategory.objects().entrySet()) {
            var expectedObject = entry.getValue();
            var object = category.getObject(entry.getKey());

            var expectedString = expectedObject.allRowsToSet().stream().map(row -> rowToMappingsString(row, expectedObject)).toList();
            var string = object.allRowsToSet().stream().map(row -> rowToMappingsString(row, object)).toList();

            assertEquals(expectedString, string);
        }
    }

    private static String rowToMappingsString(DomainRow row, InstanceObject object) {
        String output = "\n[row] (" + object.key() + ") "  + row;

        for (var mappingsOfType : row.getAllMappingsFrom()) {
            output += "\n\tmappings [" + mappingsOfType.getKey().signature() + "] --> (" + mappingsOfType.getKey().cod().key() + "):";
            for (var m : mappingsOfType.getValue())
                output += "\n\t\t" + m.codomainRow();
        }

        return output + "\n";
    }
}
