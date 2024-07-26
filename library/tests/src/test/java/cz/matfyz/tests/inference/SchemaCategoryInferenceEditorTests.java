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
import cz.matfyz.inference.edit.ClusterInferenceEdit;
import cz.matfyz.inference.edit.ReferenceMergeInferenceEdit;
import cz.matfyz.inference.edit.InferenceEditor;
import cz.matfyz.inference.edit.PrimaryKeyMergeInferenceEdit;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.wrappercsv.CsvControlWrapper;
import cz.matfyz.wrappercsv.CsvProvider;
import cz.matfyz.wrappercsv.CsvProvider.CsvSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class SchemaCategoryInferenceEditorTests {

    private static final SparkSettings sparkSettings = new SparkSettings("local[*]", "./spark");

    @Test
    void testReferenceMergeEditSchemaCategory() throws Exception {

        final var url = ClassLoader.getSystemResource("inferenceSampleGoogleApps.csv");
        final var settings = new CsvSettings(url.toURI().toString(), false, false);
        final var csvProvider = new CsvProvider(settings);

        final AbstractInferenceWrapper inferenceWrapper = new CsvControlWrapper(csvProvider).getInferenceWrapper(sparkSettings);

        final List<CategoryMappingPair> pairs = new MMInferOneInAll()
            .input(inferenceWrapper, "apps", "Test Schema Category")
            .run();

        final SchemaCategory schemaCategory = CategoryMappingPair.mergeCategory(pairs, "Test Schema Category");

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
        edit.setOldReferenceSig(Signature.createBase(2));
        edit.setNewReferenceSig(Signature.createBase(6));
        edit.setOldIndexSig(Signature.createBase(3));
        edit.setNewIndexSig(Signature.createBase(7));

        List<Mapping> mappings = new ArrayList<>();
        mappings.add(mappingA);
        mappings.add(mappingB);

        List<Mapping> editMappings = edit.applyMappingEdit(mappings);

        System.out.println();
        System.out.println("Editted Mapping: ");
        System.out.println(editMappings.size());
    }


 @Test
    void testReferenceMergeEdit() throws Exception {

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

        SchemaCategory category = new SchemaCategory("schema");
        for (SchemaObject obj : categoryA.allObjects()) {
            category.addObject(obj);
        }
        for (SchemaObject obj : categoryB.allObjects()) {
            category.addObject(obj);
        }
        for (SchemaMorphism morph : categoryA.allMorphisms()) {
            category.addMorphism(morph);
        }
        for (SchemaMorphism morph : categoryB.allMorphisms()) {
            category.addMorphism(morph);
        }

        List<Mapping> mappings = new ArrayList<>();
        mappings.add(mappingA);
        mappings.add(mappingB);

        ReferenceMergeInferenceEdit edit = new ReferenceMergeInferenceEdit(new Key(2), new Key(4));

        SchemaCategory categoryFinal = edit.applySchemaCategoryEdit(category);

        System.out.println("new reference sig: " + edit.getNewReferenceSig());
        System.out.println("old reference sig: " + edit.getOldReferenceSig());
        System.out.println("new index sig: " + edit.getNewIndexSig());
        System.out.println("old index sig: " + edit.getOldIndexSig());

        List<Mapping> editMappings = edit.applyMappingEdit(mappings);

        System.out.println();
        System.out.println("Editted Size: ");
        System.out.println(editMappings.size());
    }

    @Test
    void testPrimaryKeyMergeEdit() throws Exception {

        // Setup A
        SchemaCategory categoryA = new SchemaCategory("schemaA");
        categoryA.addObject(new SchemaObject(new Key(0), "app", null, null));
        categoryA.addObject(new SchemaObject(new Key(1), "app_id", null, null));
        categoryA.addObject(new SchemaObject(new Key(2), "name", null, null));
        categoryA.addMorphism(new SchemaMorphism(Signature.createBase(1), null, null,  new HashSet<>(), categoryA.getObject(new Key(0)), categoryA.getObject(new Key(1))));
        categoryA.addMorphism(new SchemaMorphism(Signature.createBase(2), null, null,  new HashSet<>(), categoryA.getObject(new Key(0)), categoryA.getObject(new Key(2))));

        MappingBuilder builderA = new MappingBuilder();
        List<AccessPath> subpathsA = new ArrayList<>();

        subpathsA.add(builderA.simple("name", Signature.createBase(2)));
        subpathsA.add(builderA.simple("app_id", Signature.createBase(1)));

        ComplexProperty complexPropertyA = builderA.complex("app", Signature.createBase(0), subpathsA.toArray(new AccessPath[0]));

        Mapping mappingA = new Mapping(categoryA, new Key(0), "kindNameA", complexPropertyA, null);
        System.out.println(mappingA.accessPath());

        System.out.println();

        // Setup B
        SchemaCategory categoryB = new SchemaCategory("schemaB");
        categoryB.addObject(new SchemaObject(new Key(3), "reviews", null, null));
        categoryB.addObject(new SchemaObject(new Key(4), "app_id", null, null));
        categoryB.addObject(new SchemaObject(new Key(5), "text", null, null));
        categoryB.addMorphism(new SchemaMorphism(Signature.createBase(3), null, null,  new HashSet<>(), categoryB.getObject(new Key(3)), categoryB.getObject(new Key(4))));
        categoryB.addMorphism(new SchemaMorphism(Signature.createBase(4), null, null,  new HashSet<>(), categoryB.getObject(new Key(3)), categoryB.getObject(new Key(5))));

        MappingBuilder builderB = new MappingBuilder();
        List<AccessPath> subpathsB = new ArrayList<>();

        subpathsB.add(builderB.simple("text", Signature.createBase(4)));
        subpathsB.add(builderB.simple("app_id", Signature.createBase(3)));

        ComplexProperty complexPropertyB = builderB.complex("reviews", Signature.createBase(5), subpathsB.toArray(new AccessPath[0]));

        Mapping mappingB = new Mapping(categoryB, new Key(3), "kindNameB", complexPropertyB, null);
        System.out.println(mappingB.accessPath());

        SchemaCategory category = new SchemaCategory("schema");
        for (SchemaObject obj : categoryA.allObjects()) {
            category.addObject(obj);
        }
        for (SchemaObject obj : categoryB.allObjects()) {
            category.addObject(obj);
        }
        for (SchemaMorphism morph : categoryA.allMorphisms()) {
            category.addMorphism(morph);
        }
        for (SchemaMorphism morph : categoryB.allMorphisms()) {
            category.addMorphism(morph);
        }

        List<Mapping> mappings = new ArrayList<>();
        mappings.add(mappingA);
        mappings.add(mappingB);

        PrimaryKeyMergeInferenceEdit edit = new PrimaryKeyMergeInferenceEdit(new Key(1));

        //SchemaCategory categoryFinal = edit.applySchemaCategoryEdit(category);

        //List<Mapping> editMappings = edit.applyMappingEdit(mappings, categoryFinal);
/*
        System.out.println();
        System.out.println("Editted Size: ");
        System.out.println(editMappings.size());*/
    }

    @Test
    void testClusterEdit() throws Exception {

        SchemaCategory category = new SchemaCategory("schema");
        category.addObject(new SchemaObject(new Key(0), "world", null, null));
        category.addObject(new SchemaObject(new Key(1), "continent", null, null));
        category.addObject(new SchemaObject(new Key(2), "country_1", null, null));
        category.addObject(new SchemaObject(new Key(3), "a", null, null));
        category.addObject(new SchemaObject(new Key(4), "b", null, null));
        category.addObject(new SchemaObject(new Key(5), "c", null, null));
        category.addObject(new SchemaObject(new Key(6), "country_2", null, null));
        category.addObject(new SchemaObject(new Key(7), "a", null, null));
        category.addObject(new SchemaObject(new Key(8), "b", null, null));
        category.addObject(new SchemaObject(new Key(9), "c", null, null));

        category.addMorphism(new SchemaMorphism(Signature.createBase(1), null, null,  new HashSet<>(), category.getObject(new Key(0)), category.getObject(new Key(1))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(2), null, null,  new HashSet<>(), category.getObject(new Key(1)), category.getObject(new Key(2))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(3), null, null,  new HashSet<>(), category.getObject(new Key(1)), category.getObject(new Key(6))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(4), null, null,  new HashSet<>(), category.getObject(new Key(2)), category.getObject(new Key(3))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(5), null, null,  new HashSet<>(), category.getObject(new Key(2)), category.getObject(new Key(4))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(6), null, null,  new HashSet<>(), category.getObject(new Key(3)), category.getObject(new Key(5))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(7), null, null,  new HashSet<>(), category.getObject(new Key(6)), category.getObject(new Key(7))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(8), null, null,  new HashSet<>(), category.getObject(new Key(6)), category.getObject(new Key(8))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(9), null, null,  new HashSet<>(), category.getObject(new Key(7)), category.getObject(new Key(9))));
/*
        MappingBuilder builder = new MappingBuilder();

        List<AccessPath> subpaths = new ArrayList<>();
        subpaths.add(builder.simple("c", Signature.createBase(6)));
        subpaths.add(builder.simple("b", Signature.createBase(5)));

        ComplexProperty complexProperty = builder.complex("app", Signature.createBase(0), subpaths.toArray(new AccessPath[0]));

        Mapping mapping = new Mapping(category, new Key(0), "kindNameA", complexProperty, null);
        System.out.println(mapping.accessPath());
*/
        System.out.println();

        //List<Mapping> mappings = new ArrayList<>();
        //mappings.add(mapping);
        List<Key> clusterKeys = new ArrayList<>();
        clusterKeys.add(new Key(2));
        clusterKeys.add(new Key(6));

        ClusterInferenceEdit edit = new ClusterInferenceEdit(clusterKeys);

        for (SchemaObject so : category.allObjects()) {
            System.out.println(so.label());
        }

        SchemaCategory categoryFinal = edit.applySchemaCategoryEdit(category);
/*
        for (SchemaMorphism sm : categoryFinal.allMorphisms()) {
            System.out.println("dom: " + sm.dom().label());
            System.out.println("cod: " + sm.cod().label());
            System.out.println();
        }*/

        //List<Mapping> editMappings = edit.applyMappingEdit(mappings, categoryFinal);
/*
        System.out.println();
        System.out.println("Editted Size: ");
        System.out.println(editMappings.size());*/
    }

    @Test
    void testRecursionEdit() throws Exception {

        SchemaCategory category = new SchemaCategory("schema");
        category.addObject(new SchemaObject(new Key(0), "B", null, null));
        category.addObject(new SchemaObject(new Key(1), "A", null, null));
        category.addObject(new SchemaObject(new Key(2), "A", null, null));
        category.addObject(new SchemaObject(new Key(3), "B", null, null));
        category.addObject(new SchemaObject(new Key(4), "B", null, null));
        category.addObject(new SchemaObject(new Key(5), "B", null, null));
        category.addObject(new SchemaObject(new Key(6), "A", null, null));
        category.addObject(new SchemaObject(new Key(7), "A", null, null));
        category.addObject(new SchemaObject(new Key(8), "B", null, null));
        category.addObject(new SchemaObject(new Key(9), "B", null, null));
        category.addObject(new SchemaObject(new Key(10), "A", null, null));
        category.addObject(new SchemaObject(new Key(11), "B", null, null));

        category.addMorphism(new SchemaMorphism(Signature.createBase(1), null, null,  new HashSet<>(), category.getObject(new Key(0)), category.getObject(new Key(1))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(2), null, null,  new HashSet<>(), category.getObject(new Key(0)), category.getObject(new Key(2))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(3), null, null,  new HashSet<>(), category.getObject(new Key(1)), category.getObject(new Key(3))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(4), null, null,  new HashSet<>(), category.getObject(new Key(1)), category.getObject(new Key(4))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(5), null, null,  new HashSet<>(), category.getObject(new Key(2)), category.getObject(new Key(5))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(6), null, null,  new HashSet<>(), category.getObject(new Key(3)), category.getObject(new Key(6))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(7), null, null,  new HashSet<>(), category.getObject(new Key(4)), category.getObject(new Key(7))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(8), null, null,  new HashSet<>(), category.getObject(new Key(6)), category.getObject(new Key(8))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(9), null, null,  new HashSet<>(), category.getObject(new Key(7)), category.getObject(new Key(9))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(10), null, null,  new HashSet<>(), category.getObject(new Key(9)), category.getObject(new Key(10))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(11), null, null,  new HashSet<>(), category.getObject(new Key(10)), category.getObject(new Key(11))));
/*
        MappingBuilder builder = new MappingBuilder();

        List<AccessPath> subpaths = new ArrayList<>();
        subpaths.add(builder.simple("c", Signature.createBase(6)));
        subpaths.add(builder.simple("b", Signature.createBase(5)));

        ComplexProperty complexProperty = builder.complex("app", Signature.createBase(0), subpaths.toArray(new AccessPath[0]));

        Mapping mapping = new Mapping(category, new Key(0), "kindNameA", complexProperty, null);
        System.out.println(mapping.accessPath());
*/
        System.out.println();
        System.out.println(category.hasObject(new Key(1)));
        System.out.println("ahoj");
        System.out.println(category.getObject(new Key(1)));
        System.out.println(category.allObjects());
        System.out.println(category.allObjects().size());
        System.out.println("ahoj");

        //List<Mapping> mappings = new ArrayList<>();
        //mappings.add(mapping);
    
        //ClusterInferenceEdit edit = new ClusterInferenceEdit(clusterKeys);

        for (SchemaObject so : category.allObjects()) {
            System.out.println(so.label());
        }

        //SchemaCategory categoryFinal = edit.applySchemaCategoryEdit(category);
/*
        for (SchemaMorphism sm : categoryFinal.allMorphisms()) {
            System.out.println("dom: " + sm.dom().label());
            System.out.println("cod: " + sm.cod().label());
            System.out.println();
        }*/

        //List<Mapping> editMappings = edit.applyMappingEdit(mappings, categoryFinal);
/*
        System.out.println();
        System.out.println("Editted Size: ");
        System.out.println(editMappings.size());*/
    }
}
