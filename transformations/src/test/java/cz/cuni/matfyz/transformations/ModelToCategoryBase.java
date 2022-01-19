package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.utils.Debug;
import cz.cuni.matfyz.wrapperDummy.DummyPullWrapper;

import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author pavel.koupil, jachymb.bartik
 */
public abstract class ModelToCategoryBase
{
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(ModelToCategoryBase.class);
    
    protected abstract String getFileName();

    protected int getDebugLevel()
    {
        return 5;
    }
    
    @BeforeEach
	public void setUp()
    {
        Debug.setLevel(getDebugLevel());
        UniqueIdProvider.reset();
    }

	/**
	 * Test of algorithm method of class ModelToCategory.
	 */
	public void testAlgorithm()
    {
		SchemaCategory schema = buildSchemaCategoryScenario();
        if (Debug.shouldLog(3))
        {
			System.out.println(String.format("# Schema Category\n%s", schema));
        }
        
		InstanceCategory instance = buildInstanceScenario(schema);
		ComplexProperty path = buildComplexPropertyPath(schema);
        if (Debug.shouldLog(3))
        {
			System.out.println(String.format("# Access Path\n%s", path));
        }
        
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
        {
			System.out.println(String.format("# Forest of Records\n%s", forest));
        }
        
		Mapping mapping = buildMapping(schema, path);

		ModelToCategory transformation = new ModelToCategory();
		transformation.input(schema, instance, forest, mapping);
		transformation.algorithm();

        if (Debug.shouldLog(4))
        {
			System.out.println(String.format("# Instance CategoryRecords\n%s", instance));
        }
		
        InstanceCategory expectedInstance = buildExpectedInstanceCategory(schema);
        
        Assertions.assertEquals(expectedInstance.objects(), instance.objects(), "Test objects differs from the expected objects.");
        Assertions.assertEquals(expectedInstance.morphisms(), instance.morphisms(), "Test morphisms differs from the expected morphisms.");
	}

    protected abstract SchemaCategory buildSchemaCategoryScenario();

	protected InstanceCategory buildInstanceScenario(SchemaCategory schema)
    {
        return new InstanceCategoryBuilder().setSchemaCategory(schema).build();
	}

	protected abstract ComplexProperty buildComplexPropertyPath(SchemaCategory schema);

	protected ForestOfRecords buildForestOfRecords(ComplexProperty path) throws Exception
    {
		DummyPullWrapper wrapper = new DummyPullWrapper();
        
        var url = ClassLoader.getSystemResource(getFileName());
        String fileName = Paths.get(url.toURI()).toAbsolutePath().toString();
        
		return wrapper.pullForest(fileName, path);
	}
	
	protected abstract Mapping buildMapping(SchemaCategory schema, ComplexProperty path);
    
    protected abstract InstanceCategory buildExpectedInstanceCategory(SchemaCategory schema);
}
