package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.utils.Debug;
import cz.cuni.matfyz.wrapperDummy.DMLTestStructure;
import cz.cuni.matfyz.wrapperDummy.DummyPushWrapper;

import java.nio.file.*;
import java.util.*;
import org.json.*;

import org.junit.jupiter.api.Assertions;

/**
 *
 * @author jachymb.bartik
 */
public class DMLAlgorithmTestBase
{
    private final String fileNamePrefix = "dmlAlgorithm/";
    private final String dataFileName;

    public DMLAlgorithmTestBase(String dataFileName)
    {
        this.dataFileName = dataFileName;
    }

    public DMLAlgorithmTestBase setAll(SchemaCategory schema, SchemaObject rootObject, String rootName, ComplexProperty path, InstanceCategory inputInstance)
    {
        return setSchema(schema).setRootObject(rootObject).setRootName(rootName).setPath(path).setInputInstance(inputInstance);
    }

    private SchemaCategory schema;

    public DMLAlgorithmTestBase setSchema(SchemaCategory schema)
    {
        this.schema = schema;
        
        if (Debug.shouldLog(3))
            System.out.println(String.format("# Schema Category\n%s", schema));

        return this;
    }

    private InstanceCategory inputInstance;

    public DMLAlgorithmTestBase setInputInstance(InstanceCategory inputInstance)
    {
        this.inputInstance = inputInstance;

        return this;
    }

    private SchemaObject rootObject;

    public DMLAlgorithmTestBase setRootObject(SchemaObject rootObject)
    {
        this.rootObject = rootObject;

        return this;
    }

    private String rootName;

    public DMLAlgorithmTestBase setRootName(String rootName)
    {
        this.rootName = rootName;

        return this;
    }

    private ComplexProperty path;

    public DMLAlgorithmTestBase setPath(ComplexProperty path)
    {
        this.path = path;

        if (Debug.shouldLog(3))
            System.out.println(String.format("# Access Path\n%s", path));

        return this;
    }

	private List<DMLTestStructure> buildExpectedResult() throws Exception
    {
        var url = ClassLoader.getSystemResource(fileNamePrefix + dataFileName);
        Path pathToDataFileName = Paths.get(url.toURI()).toAbsolutePath();
		String jsonString = Files.readString(pathToDataFileName);
        var json = new JSONArray(jsonString);
        var structures = new ArrayList<DMLTestStructure>();
        
        for (int i = 0; i < json.length(); i++)
            structures.add(new DMLTestStructure(json.getJSONObject(i)));
        
		return structures;
	}

	public void testAlgorithm()
    {
        List<DMLTestStructure> expectedResult;
        try
        {
            expectedResult = buildExpectedResult();
        }
        catch (Exception e)
        {
            Assertions.fail("Exception thrown when loading test data.");
            return;
        }

        var wrapper = new DummyPushWrapper();

		Mapping mapping = new Mapping(rootObject, path);

		var transformation = new DMLAlgorithm();
		transformation.input(schema, inputInstance, rootName, mapping, wrapper);
		transformation.algorithm();

        List<DMLTestStructure> result = wrapper.structures();

        System.out.println(result);

        Assertions.assertTrue(resultsEquals(expectedResult, result), "Test objects differ from the expected objects.");
	}

    private boolean resultsEquals(List<DMLTestStructure> result1, List<DMLTestStructure> result2)
    {
        if (result1.size() != result2.size())
            return false;

        for (var structure1 : result1)
        {
            boolean equals = false;
            for (var structure2 : result2)
                if (structure1.equals(structure2))
                {
                    equals = true;
                    break;
                }

            if (!equals)
                return false;
        }

        return true;
    }
}
