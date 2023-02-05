package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.wrapperdummy.DMLTestStructure;
import cz.cuni.matfyz.wrapperdummy.DummyDMLWrapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;

/**
 * @author jachymb.bartik
 */
public class DMLAlgorithmTestBase {
    
    private final String fileNamePrefix = "dmlAlgorithm/";
    private final String dataFileName;

    public DMLAlgorithmTestBase(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public DMLAlgorithmTestBase setAll(SchemaCategory schema, SchemaObject rootObject, String kindName, ComplexProperty path, InstanceCategory inputInstance) {
        return setSchema(schema).setRootObject(rootObject).setKindName(kindName).setPath(path).setInputInstance(inputInstance);
    }

    private SchemaCategory schema;

    public DMLAlgorithmTestBase setSchema(SchemaCategory schema) {
        this.schema = schema;
        return this;
    }

    private InstanceCategory inputInstance;

    public DMLAlgorithmTestBase setInputInstance(InstanceCategory inputInstance) {
        this.inputInstance = inputInstance;
        return this;
    }

    private SchemaObject rootObject;

    public DMLAlgorithmTestBase setRootObject(SchemaObject rootObject) {
        this.rootObject = rootObject;
        return this;
    }

    private String kindName;

    public DMLAlgorithmTestBase setKindName(String kindName) {
        this.kindName = kindName;
        return this;
    }

    private ComplexProperty path;

    public DMLAlgorithmTestBase setPath(ComplexProperty path) {
        this.path = path;
        return this;
    }

    private List<DMLTestStructure> buildExpectedResult() throws Exception {
        var url = ClassLoader.getSystemResource(fileNamePrefix + dataFileName);
        Path pathToDataFileName = Paths.get(url.toURI()).toAbsolutePath();
        String jsonString = Files.readString(pathToDataFileName);
        var json = new JSONArray(jsonString);
        var structures = new ArrayList<DMLTestStructure>();
        
        for (int i = 0; i < json.length(); i++)
            structures.add(new DMLTestStructure(json.getJSONObject(i)));
        
        return structures;
    }

    public void testAlgorithm() {
        List<DMLTestStructure> expectedResult;
        try {
            expectedResult = buildExpectedResult();
        }
        catch (Exception e) {
            Assertions.fail("Exception thrown when loading test data.");
            return;
        }

        var wrapper = new DummyDMLWrapper();

        Mapping mapping = Mapping.fromArguments(schema, rootObject, path, kindName, null);

        var transformation = new DMLAlgorithm();
        transformation.input(mapping, inputInstance, wrapper);
        transformation.algorithm();

        List<DMLTestStructure> result = wrapper.structures();

        Assertions.assertTrue(resultsEquals(expectedResult, result), "Test objects differ from the expected objects.");
    }

    private static boolean resultsEquals(List<DMLTestStructure> result1, List<DMLTestStructure> result2) {
        if (result1.size() != result2.size())
            return false;

        for (var structure1 : result1) {
            boolean equals = false;
            for (var structure2 : result2)
                if (structure1.equals(structure2)) {
                    equals = true;
                    break;
                }

            if (!equals)
                return false;
        }

        return true;
    }
}
