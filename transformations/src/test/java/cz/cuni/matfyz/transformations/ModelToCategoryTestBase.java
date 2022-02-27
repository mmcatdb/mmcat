package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.abstractwrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.utils.Debug;
import cz.cuni.matfyz.wrapperDummy.DummyPullWrapper;

import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;

/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategoryTestBase
{
    private final String fileNamePrefix = "modelToCategory/";
    private final String dataFileName;

    public ModelToCategoryTestBase(String dataFileName)
    {
        this.dataFileName = dataFileName;
    }

    public ModelToCategoryTestBase setAll(SchemaCategory schema, SchemaObject rootObject, ComplexProperty path, InstanceCategory expectedInstance)
    {
        return setSchema(schema).setRootObject(rootObject).setPath(path).setExpectedInstance(expectedInstance);
    }

    private SchemaCategory schema;

    public ModelToCategoryTestBase setSchema(SchemaCategory schema)
    {
        this.schema = schema;
        
        if (Debug.shouldLog(3))
            System.out.println(String.format("# Schema Category\n%s", schema));

        return this;
    }

    private InstanceCategory expectedInstance;

    public ModelToCategoryTestBase setExpectedInstance(InstanceCategory expectedInstance)
    {
        this.expectedInstance = expectedInstance;

        return this;
    }

    private SchemaObject rootObject;

    public ModelToCategoryTestBase setRootObject(SchemaObject rootObject)
    {
        this.rootObject = rootObject;

        return this;
    }

    private ComplexProperty path;

    public ModelToCategoryTestBase setPath(ComplexProperty path)
    {
        this.path = path;

        if (Debug.shouldLog(3))
            System.out.println(String.format("# Access Path\n%s", path));

        return this;
    }

	private ForestOfRecords buildForestOfRecords(ComplexProperty path) throws Exception
    {
		var wrapper = new DummyPullWrapper();
        
        var url = ClassLoader.getSystemResource(fileNamePrefix + dataFileName);
        String fileName = Paths.get(url.toURI()).toAbsolutePath().toString();
        
		return wrapper.pullForest(path, new PullWrapperOptions.Builder().buildWithKindName(fileName));
	}

	public void testAlgorithm()
    {
        InstanceCategory instance = new InstanceCategoryBuilder().setSchemaCategory(schema).build();

        ForestOfRecords forest;
        try
        {
		    forest = buildForestOfRecords(path);
        }
        catch (Exception e)
        {
            Assertions.fail("Exception thrown when building forest.");
            return;
        }

        if (Debug.shouldLog(3))
			System.out.println(String.format("# Forest of Records\n%s", forest));
        
		var mapping = new Mapping(rootObject, path);

		var transformation = new ModelToCategory();
		transformation.input(schema, instance, forest, mapping);
		transformation.algorithm();

        if (Debug.shouldLog(4))
			System.out.println(String.format("# Instance CategoryRecords\n%s", instance));
        
        Assertions.assertEquals(expectedInstance.objects(), instance.objects(), "Test objects differ from the expected objects.");
        Assertions.assertEquals(expectedInstance.morphisms(), instance.morphisms(), "Test morphisms differ from the expected morphisms.");
	}
}
