package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.abstractwrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceCategoryBuilder;
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
            Assertions.fail("Exception thrown when building forest.");
            return;
        }

        Mapping mapping = new Mapping.Builder().fromArguments(schema, rootObject, null, path, null, null);

        var transformation = new MTCAlgorithm();
        transformation.input(mapping, category, forest);
        transformation.algorithm();

        Assertions.assertEquals(expectedCategory.objects(), category.objects(), "Test objects differ from the expected objects.");
        Assertions.assertEquals(expectedCategory.morphisms(), category.morphisms(), "Test morphisms differ from the expected morphisms.");
    }
}
