package cz.matfyz.tests.inference;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.wrappercsv.CsvControlWrapper;
import cz.matfyz.wrappercsv.CsvProvider;
import cz.matfyz.wrappercsv.CsvProvider.CsvSettings;
import cz.matfyz.wrapperjson.JsonControlWrapper;
import cz.matfyz.wrapperjson.JsonProvider;
import cz.matfyz.wrapperjson.JsonProvider.JsonSettings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SchemaConversionTests {

    private static final SparkSettings sparkSettings = new SparkSettings("local[*]", "./spark");

    @Test
    void testBasicRSDToSchemaCategoryAndMapping() throws Exception {
        final var url = ClassLoader.getSystemResource("inferenceSampleGoogleApps.csv");
        final var settings = new CsvSettings(url.toURI().toString(), false, false);
        final var csvProvider = new CsvProvider(settings);

        final AbstractInferenceWrapper inferenceWrapper = new CsvControlWrapper(csvProvider).getInferenceWrapper(sparkSettings);

        final CategoryMappingPair categoryMappingPair = new MMInferOneInAll()
            .input(inferenceWrapper, "apps", "Test Schema Category")
            .run();

        final SchemaCategory schemaCategory = categoryMappingPair.schemaCategory();
        final Mapping mapping = categoryMappingPair.mapping();

        assertEquals(10, schemaCategory.allObjects().size(), "There should be 10 Schema Objects.");
        assertEquals(9, schemaCategory.allMorphisms().size(), "There should be 10 Schema Morphisms.");

        assertEquals(mapping.accessPath().subpaths().size(), schemaCategory.allObjects().size() - 1, "Mapping should be as long as there are Schema Objects.");
    }

    @Test
    void testComplexRSDToSchemaCategoryAndMapping() throws Exception {
        final var url = ClassLoader.getSystemResource("inferenceSampleYelp.json");
        final var settings = new JsonSettings(url.toURI().toString(), false, false);
        final var jsonProvider = new JsonProvider(settings);

        final AbstractInferenceWrapper inferenceWrapper = new JsonControlWrapper(jsonProvider).getInferenceWrapper(sparkSettings);

        final CategoryMappingPair categoryMappingPair = new MMInferOneInAll()
            .input(inferenceWrapper, "business", "Test Schema Category")
            .run();

        final SchemaCategory schemaCategory = categoryMappingPair.schemaCategory();
        final Mapping mapping = categoryMappingPair.mapping();

        assertEquals(22, schemaCategory.allObjects().size(), "There should be 10 Schema Objects.");
        assertEquals(21, schemaCategory.allMorphisms().size(), "There should be 10 Schema Morphisms.");

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
