package cz.matfyz.tests.inference;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.inference.edit.AbstractInferenceEdit;
import cz.matfyz.inference.edit.ReferenceMergeInferenceEdit;
import cz.matfyz.inference.edit.SchemaCategoryInferenceEditor;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.wrappercsv.CsvControlWrapper;
import cz.matfyz.wrappercsv.CsvProvider;
import cz.matfyz.wrappercsv.CsvProvider.CsvSettings;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class SchemaCategoryInferenceEditorTests {

    private static final SparkSettings sparkSettings = new SparkSettings("local[*]", "./spark");

    @Test
    void testReferenceMergeEdit() throws Exception {

        final var url = ClassLoader.getSystemResource("inferenceSampleGoogleApps.csv");
        final var settings = new CsvSettings(url.toURI().toString(), false, false);
        final var csvProvider = new CsvProvider(settings);

        final AbstractInferenceWrapper inferenceWrapper = new CsvControlWrapper(csvProvider).getInferenceWrapper(sparkSettings);

        final CategoryMappingPair categoryMappingPair = new MMInferOneInAll()
            .input(inferenceWrapper, "apps", "Test Schema Category")
            .run();

        final SchemaCategory schemaCategory = categoryMappingPair.schemaCategory();
        final Mapping mapping = categoryMappingPair.mapping();

        AbstractInferenceEdit edit = new ReferenceMergeInferenceEdit(new Key(1), new Key(2));

        SchemaCategory editSchemaCategory = edit.applyEdit(schemaCategory);
        System.out.println("size original: " + schemaCategory.allMorphisms().size());
        System.out.println("size edit: " + editSchemaCategory.allMorphisms().size());
    }
}
