package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.wrapperdummy.DummyICWrapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;

/**
 * @author jachymb.bartik
 */
public class ICAlgorithmTestBase {
    
    private final String fileNamePrefix = "icAlgorithm/";
    private SchemaCategory schema;
    private final Map<String, Mapping> mappings = new TreeMap<>();

    public ICAlgorithmTestBase(SchemaCategory schema) {
        this.schema = schema;
    }

    public ICAlgorithmTestBase addMappingForTest(SchemaObject rootObject, String kindName, ComplexProperty path) {
        final var mapping = new Mapping.Builder().fromArguments(schema, rootObject, null, path, kindName, primaryKeyFromObject(rootObject));
        mappings.put(kindName, mapping);

        return this;
    }

    private static List<Signature> primaryKeyFromObject(SchemaObject object) {
        if (object.ids().isSignatures())
            return object.ids().toSignatureIds().first().signatures().stream().toList();

        throw new UnsupportedOperationException("Primary key cannot be empty!");
    }

    private List<String> buildExpectedResult(String dataFileName) throws Exception {
        var url = ClassLoader.getSystemResource(fileNamePrefix + dataFileName);
        Path pathToDataFile = Paths.get(url.toURI()).toAbsolutePath();
        String jsonString = Files.readString(pathToDataFile);
        var json = new JSONArray(jsonString);
        var lines = new ArrayList<String>();
        
        for (int i = 0; i < json.length(); i++)
            lines.add(json.getString(i));
        
        return lines;
    }

    public void testAlgorithm(String dataFileName, String kindName) {
        final List<String> expectedResult;
        try {
            expectedResult = buildExpectedResult(dataFileName);
        }
        catch (Exception e) {
            Assertions.fail("Exception thrown when loading test data.");
            return;
        }

        final var wrapper = new DummyICWrapper();
        final var mapping = mappings.get(kindName);

        final var transformation = new ICAlgorithm();
        final var allMappings = ICAlgorithm.createAllMappings(mappings.values());
        transformation.input(mapping, allMappings, wrapper);
        transformation.algorithm();

        final List<String> result = wrapper.methods();

        System.out.println("\n[Expected]:");
        System.out.println(expectedResult);
        System.out.println("\n[Actual]:");
        System.out.println(result);

        Assertions.assertTrue(resultsEquals(expectedResult, result), "Test objects differ from the expected objects.");
    }

    private static boolean resultsEquals(List<String> result1, List<String> result2) {
        if (result1.size() != result2.size())
            return false;

        var set1 = new TreeSet<>(result1);
        var set2 = new TreeSet<>(result2);

        return set1.equals(set2);
    }
}
