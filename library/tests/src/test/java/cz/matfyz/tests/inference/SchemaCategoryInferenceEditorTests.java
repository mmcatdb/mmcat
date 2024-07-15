package cz.matfyz.tests.inference;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.MappingBuilder;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.inference.edit.AbstractInferenceEdit;
import cz.matfyz.inference.edit.ReferenceMergeInferenceEdit;
import cz.matfyz.inference.edit.InferenceEditor;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.wrappercsv.CsvControlWrapper;
import cz.matfyz.wrappercsv.CsvProvider;
import cz.matfyz.wrappercsv.CsvProvider.CsvSettings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class SchemaCategoryInferenceEditorTests {

    private static final SparkSettings sparkSettings = new SparkSettings("local[*]", "./spark");

    @Test
    void testMappingSerialization() throws Exception {

        final var url = ClassLoader.getSystemResource("inferenceSampleGoogleApps.csv");
        final var settings = new CsvSettings(url.toURI().toString(), false, false);
        final var csvProvider = new CsvProvider(settings);

        final AbstractInferenceWrapper inferenceWrapper = new CsvControlWrapper(csvProvider).getInferenceWrapper(sparkSettings);

        final CategoryMappingPair categoryMappingPair = new MMInferOneInAll()
            .input(inferenceWrapper, "apps", "Test Schema Category")
            .run();

        final Mapping mapping = categoryMappingPair.mapping();
        
        //String mappingString = mapping.toJsonValue();
        //System.out.println(mappingString);
    }

    @Test
    void testReferenceMergeEditSchemaCategory() throws Exception {

        final var url = ClassLoader.getSystemResource("inferenceSampleGoogleApps.csv");
        final var settings = new CsvSettings(url.toURI().toString(), false, false);
        final var csvProvider = new CsvProvider(settings);

        final AbstractInferenceWrapper inferenceWrapper = new CsvControlWrapper(csvProvider).getInferenceWrapper(sparkSettings);

        final CategoryMappingPair categoryMappingPair = new MMInferOneInAll()
            .input(inferenceWrapper, "apps", "Test Schema Category")
            .run();

        final SchemaCategory schemaCategory = categoryMappingPair.schemaCategory();

        AbstractInferenceEdit edit = new ReferenceMergeInferenceEdit(new Key(1), new Key(2));

        SchemaCategory editSchemaCategory = edit.applySchemaCategoryEdit(schemaCategory);
        System.out.println("size original: " + schemaCategory.allMorphisms().size());
        System.out.println("size edit: " + editSchemaCategory.allMorphisms().size());
    }

    @Test
    void testReferenceMergeEditMapping() throws Exception {

        // Setup A
        SchemaCategory categoryA = new SchemaCategory("schemaA");
        categoryA.addObject(new SchemaObject(new Key(0), "app", null, null));
        categoryA.addObject(new SchemaObject(new Key(1), "name", null, null));
        categoryA.addObject(new SchemaObject(new Key(2), "reviews", null, null));
        categoryA.addObject(new SchemaObject(new Key(3), "_index", null, null));
        categoryA.addMorphism(new SchemaMorphism(Signature.createBase(1), null, null,  new HashSet<>(), categoryA.getObject(new Key(0)), categoryA.getObject(new Key(1))));
        categoryA.addMorphism(new SchemaMorphism(Signature.createBase(2), null, null,  new HashSet<>(), categoryA.getObject(new Key(2)), categoryA.getObject(new Key(0))));
        categoryA.addMorphism(new SchemaMorphism(Signature.createBase(3), null, null,  new HashSet<>(), categoryA.getObject(new Key(2)), categoryA.getObject(new Key(3))));

        MappingBuilder builderA = new MappingBuilder();
        List<AccessPath> subpathsA = new ArrayList<>();

        subpathsA.add(builderA.simple("name", Signature.createBase(1)));
        subpathsA.add(builderA.complex("reviews", Signature.createBase(-2), builderA.simple("_index", Signature.createBase(3))));

        ComplexProperty complexPropertyA = builderA.complex("app", Signature.createBase(0), subpathsA.toArray(new AccessPath[0]));

        Mapping mappingA = new Mapping(categoryA, new Key(1), "kindNameA", complexPropertyA, null);
        System.out.println(mappingA.accessPath());

        System.out.println();

        // Setup B
        SchemaCategory categoryB = new SchemaCategory("schemaB");
        categoryB.addObject(new SchemaObject(new Key(4), "reviews", null, null));
        categoryB.addObject(new SchemaObject(new Key(5), "text", null, null));
        categoryB.addMorphism(new SchemaMorphism(Signature.createBase(5), null, null,  new HashSet<>(), categoryB.getObject(new Key(4)), categoryB.getObject(new Key(5))));

        MappingBuilder builderB = new MappingBuilder();
        List<AccessPath> subpathsB = new ArrayList<>();

        subpathsB.add(builderB.simple("text", Signature.createBase(5)));

        ComplexProperty complexPropertyB = builderB.complex("reviews", Signature.createBase(4), subpathsB.toArray(new AccessPath[0]));

        Mapping mappingB = new Mapping(categoryB, new Key(4), "kindNameB", complexPropertyB, null);
        System.out.println(mappingB.accessPath());

        // Setup Final
        SchemaCategory categoryFinal = new SchemaCategory("schemaFinal");

        categoryFinal.addObject(new SchemaObject(new Key(0), "app", null, null));
        categoryFinal.addObject(new SchemaObject(new Key(1), "name", null, null));
        categoryFinal.addObject(new SchemaObject(new Key(4), "reviews", null, null));
        categoryFinal.addObject(new SchemaObject(new Key(3), "_index", null, null));
        categoryFinal.addObject(new SchemaObject(new Key(5), "text", null, null));
        categoryFinal.addMorphism(new SchemaMorphism(Signature.createBase(1), null, null,  new HashSet<>(), categoryFinal.getObject(new Key(0)), categoryFinal.getObject(new Key(1))));
        categoryFinal.addMorphism(new SchemaMorphism(Signature.createBase(6), null, null,  new HashSet<>(), categoryFinal.getObject(new Key(4)), categoryFinal.getObject(new Key(0))));
        categoryFinal.addMorphism(new SchemaMorphism(Signature.createBase(7), null, null,  new HashSet<>(), categoryFinal.getObject(new Key(4)), categoryFinal.getObject(new Key(3))));
        categoryFinal.addMorphism(new SchemaMorphism(Signature.createBase(5), null, null,  new HashSet<>(), categoryFinal.getObject(new Key(4)), categoryFinal.getObject(new Key(5))));

        ReferenceMergeInferenceEdit edit = new ReferenceMergeInferenceEdit(new Key(2), new Key(4));
        edit.setOldReferenceSig(Signature.createBase(-2));
        edit.setNewReferenceSig(Signature.createBase(6));
        edit.setOldIndexSig(Signature.createBase(3));
        edit.setNewReferenceSig(Signature.createBase(7));

        List<Mapping> mappings = new ArrayList<>();
        mappings.add(mappingA);
        mappings.add(mappingB);

        List<Mapping> editMappings = edit.applyMappingEdit(mappings, categoryFinal);

        System.out.println();
        System.out.println("Editted Mapping: ");
        System.out.println(editMappings.size());
        //System.out.println(editMapping.accessPath());

    }
}
