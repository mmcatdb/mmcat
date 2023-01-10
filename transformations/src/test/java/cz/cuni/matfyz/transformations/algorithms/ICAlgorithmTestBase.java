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
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;

/**
 * @author jachymb.bartik
 */
public class ICAlgorithmTestBase {
    
    private final String fileNamePrefix = "icAlgorithm/";
    private final String dataFileName;
    
    public ICAlgorithmTestBase(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    private Mapping mapping;

    public ICAlgorithmTestBase setPrimaryMapping(Mapping mapping) {
        this.mapping = mapping;
        selectedMappings.add(mapping);
        return this;
    }

    private final Set<Mapping> selectedMappings = new TreeSet<>();

    public ICAlgorithmTestBase addOtherMappings(Mapping... mappings) {
        selectedMappings.addAll(List.of(mappings));
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
        final List<String> expectedResult;
        try {
            expectedResult = buildExpectedResult();
        }
        catch (Exception e) {
            Assertions.fail("Exception thrown when loading test data.");
            return;
        }

        final var wrapper = new DummyICWrapper();

        final var transformation = new ICAlgorithm();
        transformation.input(mapping, selectedMappings, wrapper);
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

    public static Mapping createMapping(SchemaCategory schema, SchemaObject rootObject, String kindName, ComplexProperty path) {
        return new Mapping.Builder().fromArguments(schema, rootObject, null, path, kindName, primaryKeyFromObject(rootObject));
    }

    private static List<Signature> primaryKeyFromObject(SchemaObject object) {
        if (object.ids().isSignatures())
            return object.ids().toSignatureIds().first().signatures().stream().toList();

        throw new UnsupportedOperationException("Primary key cannot be empty!");
    }

}
