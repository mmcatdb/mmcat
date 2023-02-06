package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.wrapperdummy.DummyDDLWrapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;


/**
 * @author jachymb.bartik
 */
public class DDLAlgorithmTestBase {

    private final String fileNamePrefix = "ddlAlgorithm/";
    private final String dataFileName;

    public DDLAlgorithmTestBase(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public DDLAlgorithmTestBase setAll(SchemaCategory schema, SchemaObject rootObject, String kindName, ComplexProperty path, InstanceCategory inputInstance) {
        return setSchema(schema).setRootObject(rootObject).setKindName(kindName).setPath(path).setInputInstance(inputInstance);
    }

    private SchemaCategory schema;

    public DDLAlgorithmTestBase setSchema(SchemaCategory schema) {
        this.schema = schema;
        return this;
    }

    private InstanceCategory inputInstance;

    public DDLAlgorithmTestBase setInputInstance(InstanceCategory inputInstance) {
        this.inputInstance = inputInstance;
        return this;
    }

    private SchemaObject rootObject;

    public DDLAlgorithmTestBase setRootObject(SchemaObject rootObject) {
        this.rootObject = rootObject;
        return this;
    }

    private String kindName;

    public DDLAlgorithmTestBase setKindName(String kindName) {
        this.kindName = kindName;
        return this;
    }

    private ComplexProperty path;

    public DDLAlgorithmTestBase setPath(ComplexProperty path) {
        this.path = path;
        return this;
    }

    private List<String> buildExpectedResult() throws Exception {
        var url = ClassLoader.getSystemResource(fileNamePrefix + dataFileName);
        Path pathToDataFile = Paths.get(url.toURI()).toAbsolutePath();
        String jsonString = Files.readString(pathToDataFile);
        var json = new JSONArray(jsonString);
        var lines = new ArrayList<String>();
        
        for (int i = 0; i < json.length(); i++)
            lines.add(json.getString(i));
        
        return lines;
    }

    public void testAlgorithm() {
        List<String> expectedResult;
        try {
            expectedResult = buildExpectedResult();
        }
        catch (Exception e) {
            Assertions.fail("Exception thrown when loading test data.");
            return;
        }

        var wrapper = new DummyDDLWrapper();

        Mapping mapping = new Mapping(schema, rootObject, path, kindName, null);

        var transformation = new DDLAlgorithm();
        transformation.input(mapping, inputInstance, wrapper);
        transformation.algorithm();

        List<String> result = wrapper.methods();

        Assertions.assertTrue(resultsEquals(expectedResult, result), "Test objects differ from the expected objects.");
    }

    /*
    private static void printResult(List<String> result) {
        var builder = new StringBuilder();

        builder.append("[\n");
        result.forEach(line -> builder.append("    ").append(line).append("\n"));
        builder.append("]");

        System.out.println(builder.toString());
    }
    */

    private static boolean resultsEquals(List<String> result1, List<String> result2) {
        if (result1.size() != result2.size())
            return false;

        var set1 = new TreeSet<>(result1);
        var set2 = new TreeSet<>(result2);

        return set1.equals(set2);
    }
}
