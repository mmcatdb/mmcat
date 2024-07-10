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
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.matfyz.wrappermongodb.MongoDBInferenceWrapper;
import cz.matfyz.wrappermongodb.MongoDBProvider;
import cz.matfyz.wrappermongodb.MongoDBProvider.MongoDBSettings;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class SchemaCategoryInferenceEditorTests {

    private static final SparkSettings sparkSettings = new SparkSettings("local[*]", "./spark");

    @Test
    void testInferenceEditApplication() throws Exception {

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
        List<AbstractInferenceEdit> edits = List.of(edit);
        SchemaCategoryInferenceEditor editor = new SchemaCategoryInferenceEditor(schemaCategory, edits);
        editor.applyEdits();
        System.out.println(editor.getSchemaCategory().allMorphisms());
    }
}
