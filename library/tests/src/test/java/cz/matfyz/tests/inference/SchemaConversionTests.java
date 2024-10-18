package cz.matfyz.tests.inference;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.inference.schemaconversion.AccessTreeToSchemaCategoryConverter;
import cz.matfyz.inference.schemaconversion.RSDToAccessTreeConverter;
import cz.matfyz.inference.schemaconversion.utils.AccessTreeNode;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.inference.schemaconversion.utils.InferenceResult;
import cz.matfyz.inference.schemaconversion.utils.UniqueNumberGenerator;
import cz.matfyz.tests.example.common.SparkProvider;
import cz.matfyz.wrappercsv.CsvControlWrapper;
import cz.matfyz.wrappercsv.CsvProvider;
import cz.matfyz.wrappercsv.CsvProvider.CsvSettings;
import cz.matfyz.wrapperjson.JsonControlWrapper;
import cz.matfyz.wrapperjson.JsonProvider;
import cz.matfyz.wrapperjson.JsonProvider.JsonSettings;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class SchemaConversionTests {

    private final SparkProvider sparkProvider = new SparkProvider();

    @Test
    void testRSDToAccessTree() throws Exception {
        final var url = ClassLoader.getSystemResource("inferenceSampleYelpSimple.json");
        final var settings = new JsonSettings(url.toURI().toString(), false, false);
        final var jsonProvider = new JsonProvider(settings);

        final AbstractInferenceWrapper inferenceWrapper = new JsonControlWrapper(jsonProvider).getInferenceWrapper(sparkProvider.getSettings());

        // accessing the private method with reflection w/o having to make it visible
        final Method privateExecuteRBA = MMInferOneInAll.class.getDeclaredMethod("executeRBA", AbstractInferenceWrapper.class, boolean.class);
        privateExecuteRBA.setAccessible(true);

        final MMInferOneInAll mmInferOneInAll = new MMInferOneInAll();
        final var rsd = (RecordSchemaDescription) privateExecuteRBA.invoke(mmInferOneInAll, inferenceWrapper, false);

        final RSDToAccessTreeConverter rsdToAccessTreeConverter = new RSDToAccessTreeConverter("business", new UniqueNumberGenerator(0), new UniqueNumberGenerator(0));
        final AccessTreeNode root = rsdToAccessTreeConverter.convert(rsd);

        // capture the output of the printTree method
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            root.printTree(" ");
        } finally {
            System.setOut(originalOut);
        }

        String treeOutput = outputStream.toString();

        String expectedTreeStructure = """
                                        Name: business, State: ROOT, Signature: None, Key: 0, Parent Key: null, isArrayType: false
                                             Name: address, State: SIMPLE, Signature: 0, Key: 1, Parent Key: 0, isArrayType: false
                                             Name: business_id, State: SIMPLE, Signature: 1, Key: 2, Parent Key: 0, isArrayType: false
                                             Name: hours, State: COMPLEX, Signature: 2, Key: 3, Parent Key: 0, isArrayType: false
                                                 Name: Friday, State: SIMPLE, Signature: 3, Key: 4, Parent Key: 3, isArrayType: false
                                                 Name: Saturday, State: SIMPLE, Signature: 4, Key: 5, Parent Key: 3, isArrayType: false
                                             Name: name, State: SIMPLE, Signature: 5, Key: 6, Parent Key: 0, isArrayType: false""";

        assertEquals(expectedTreeStructure, treeOutput.trim());
    }

    @Test
    void testAccessTreeToSchemaCategory() throws Exception {
        AccessTreeNode root = new AccessTreeNode(AccessTreeNode.State.ROOT, "person", null, new Key(0), null, null, null, false);
        AccessTreeNode child1 = new AccessTreeNode(AccessTreeNode.State.SIMPLE, "name", Signature.createBase(0), new Key(1), new Key(0), null, null, false);
        AccessTreeNode child2 = new AccessTreeNode(AccessTreeNode.State.COMPLEX, "adress", Signature.createBase(1), new Key(2), new Key(0), null, null, false);
        AccessTreeNode grandChild1 = new AccessTreeNode(AccessTreeNode.State.SIMPLE, "city", Signature.createBase(2), new Key(3), new Key(2), null, null, false);
        AccessTreeNode grandChild2 = new AccessTreeNode(AccessTreeNode.State.SIMPLE, "street", Signature.createBase(3), new Key(4), new Key(2), null, null, false);

        child2.addChild(grandChild1);
        child2.addChild(grandChild2);
        root.addChild(child1);
        root.addChild(child2);

        final AccessTreeToSchemaCategoryConverter accessTreeToSchemaCategoryConverter = new AccessTreeToSchemaCategoryConverter("person");
        final SchemaCategory schema = accessTreeToSchemaCategoryConverter.convert(root).schema();

        assertEquals(5, schema.allObjects().size());
        assertEquals(4, schema.allMorphisms().size());

        assertNotNull(schema.getObject(new Key(0)), "Key 0 should be present in the schema category");
        assertNotNull(schema.getObject(new Key(1)), "Key 1 should be present in the schema category");
        assertNotNull(schema.getObject(new Key(2)), "Key 2 should be present in the schema category");
        assertNotNull(schema.getObject(new Key(3)), "Key 3 should be present in the schema category");
        assertNotNull(schema.getObject(new Key(4)), "Key 4 should be present in the schema category");

        assertNull(schema.getObject(new Key(5)), "Key 5 should not be present in the schema category");
    }


    @Test
    void testBasicRSDToSchemaCategoryAndMapping() throws Exception {
        final var url = ClassLoader.getSystemResource("inferenceSampleGoogleApps.csv");
        final var settings = new CsvSettings(url.toURI().toString(), false, false);
        final var csvProvider = new CsvProvider(settings);

        final AbstractInferenceWrapper inferenceWrapper = new CsvControlWrapper(csvProvider).getInferenceWrapper(sparkProvider.getSettings());

        final InferenceResult inferenceResult = new MMInferOneInAll()
            .input(inferenceWrapper)
            .run();

        final List<CategoryMappingPair> pairs = inferenceResult.pairs();

        final var pair = CategoryMappingPair.merge(pairs);
        final SchemaCategory schema = pair.schema();
        final Mapping mapping = pair.mappings().get(0);

        assertEquals(10, schema.allObjects().size(), "There should be 10 Schema Objects.");
        assertEquals(9, schema.allMorphisms().size(), "There should be 10 Schema Morphisms.");

        assertEquals(mapping.accessPath().subpaths().size(), schema.allObjects().size() - 1, "Mapping should be as long as there are Schema Objects.");
    }

    @Test
    void testComplexRSDToSchemaCategoryAndMapping() throws Exception {
        final var url = ClassLoader.getSystemResource("inferenceSampleYelp.json");
        final var settings = new JsonSettings(url.toURI().toString(), false, false);
        final var jsonProvider = new JsonProvider(settings);

        final AbstractInferenceWrapper inferenceWrapper = new JsonControlWrapper(jsonProvider).getInferenceWrapper(sparkProvider.getSettings());

        final InferenceResult inferenceResult = new MMInferOneInAll()
            .input(inferenceWrapper)
            .run();

        final List<CategoryMappingPair> pairs = inferenceResult.pairs();

        final var pair = CategoryMappingPair.merge(pairs);
        final SchemaCategory schema = pair.schema();
        final Mapping mapping = pair.mappings().get(0);

        assertEquals(22, schema.allObjects().size(), "There should be 10 Schema Objects.");
        assertEquals(21, schema.allMorphisms().size(), "There should be 10 Schema Morphisms.");

        assertEquals(3, countComplexProperties(mapping), "There should be 3 complex properties");
    }

   public static int countComplexProperties(Mapping mapping) {
    return countComplexPropertiesRecursive(mapping.accessPath());
    }

    private static int countComplexPropertiesRecursive(ComplexProperty property) {
        int count = 1; // count this ComplexProperty itself
        for (AccessPath subpath : property.subpaths()) {
            if (subpath instanceof ComplexProperty) {
                count += countComplexPropertiesRecursive((ComplexProperty) subpath);
            }
        }
        return count;
    }

}
