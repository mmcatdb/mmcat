package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.utils.Debug;
import cz.cuni.matfyz.wrapperDummy.DummyDDLWrapper;

import java.nio.file.*;
import java.util.*;
import org.json.*;

import org.junit.jupiter.api.Assertions;

/**
 *
 * @author jachymb.bartik
 */
public class DDLAlgorithmTestBase
{
    private final String fileNamePrefix = "ddlAlgorithm/";
    private final String dataFileName;

    public DDLAlgorithmTestBase(String dataFileName)
    {
        this.dataFileName = dataFileName;
    }

    public DDLAlgorithmTestBase setAll(SchemaCategory schema, String rootName, ComplexProperty path, InstanceCategory inputInstance)
    {
        return setSchema(schema).setRootName(rootName).setPath(path).setInputInstance(inputInstance);
    }

    private SchemaCategory schema;

    public DDLAlgorithmTestBase setSchema(SchemaCategory schema)
    {
        this.schema = schema;
        
        if (Debug.shouldLog(3))
            System.out.println(String.format("# Schema Category\n%s", schema));

        return this;
    }

    private InstanceCategory inputInstance;

    public DDLAlgorithmTestBase setInputInstance(InstanceCategory inputInstance)
    {
        this.inputInstance = inputInstance;

        return this;
    }

    private String rootName;

    public DDLAlgorithmTestBase setRootName(String rootName)
    {
        this.rootName = rootName;

        return this;
    }

    private ComplexProperty path;

    public DDLAlgorithmTestBase setPath(ComplexProperty path)
    {
        this.path = path;

        if (Debug.shouldLog(3))
            System.out.println(String.format("# Access Path\n%s", path));

        return this;
    }

	private List<String> buildExpectedResult() throws Exception
    {
        var url = ClassLoader.getSystemResource(fileNamePrefix + dataFileName);
        Path pathToDataFile = Paths.get(url.toURI()).toAbsolutePath();
		String jsonString = Files.readString(pathToDataFile);
        var json = new JSONArray(jsonString);
        var lines = new ArrayList<String>();
        
        for (int i = 0; i < json.length(); i++)
            lines.add(json.getString(i));
        
		return lines;
	}

	public void testAlgorithm()
    {
        List<String> expectedResult;
        try
        {
            expectedResult = buildExpectedResult();
        }
        catch (Exception e)
        {
            Assertions.fail("Exception thrown when loading test data.");
            return;
        }

        var wrapper = new DummyDDLWrapper();

		var transformation = new DDLAlgorithm();
		transformation.input(schema, inputInstance, rootName, path, wrapper);
		transformation.algorithm();

        List<String> result = wrapper.methods();

        printResult(result);

        Assertions.assertTrue(resultsEquals(expectedResult, result), "Test objects differ from the expected objects.");
	}

    private static void printResult(List<String> result)
    {
        var builder = new StringBuilder();

        builder.append("[\n");
        result.forEach(line -> builder.append("    ").append(line).append("\n"));
        builder.append("]");

        System.out.println(builder.toString());
    }

    private static boolean resultsEquals(List<String> result1, List<String> result2)
    {
        if (result1.size() != result2.size())
            return false;

        var set1 = new TreeSet<>(result1);
        var set2 = new TreeSet<>(result2);

        return set1.equals(set2);
    }
}
