package cz.matfyz.tests.inference;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.MappingBuilder;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.edit.PatternSegment;
import cz.matfyz.inference.edit.algorithms.ClusterMerge;
import cz.matfyz.inference.edit.algorithms.PrimaryKeyMerge;
import cz.matfyz.inference.edit.algorithms.RecursionMerge;
import cz.matfyz.inference.edit.algorithms.ReferenceMerge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

class SchemaCategoryInferenceEditorTests {

    @Test
    void testReferenceMergeEditArray() {
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

        ReferenceMerge edit = (new ReferenceMerge.Data(new Key(2), new Key(4))).createAlgorithm();

        SchemaCategory categoryFinal = edit.applySchemaCategoryEdit(category);

        System.out.println("Schema Category after edit:");
        System.out.println("Objects: " + categoryFinal.allObjects());
        for (SchemaMorphism m : categoryFinal.allMorphisms()) {
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature());
        }

        List<Mapping> editMappings = edit.applyMappingEdit(mappings);

        System.out.println(editMappings.get(0).accessPath());
    }

    @Test
    void testReferenceMergeEditObject() {

        // Setup A
        SchemaCategory categoryA = new SchemaCategory("schemaA");
        categoryA.addObject(new SchemaObject(new Key(0), "app", null, null));
        categoryA.addObject(new SchemaObject(new Key(1), "name", null, null));
        categoryA.addObject(new SchemaObject(new Key(2), "reviews", null, null));
        categoryA.addMorphism(new SchemaMorphism(Signature.createBase(1), null, null,  new HashSet<>(), categoryA.getObject(new Key(0)), categoryA.getObject(new Key(1))));
        categoryA.addMorphism(new SchemaMorphism(Signature.createBase(2), null, null,  new HashSet<>(), categoryA.getObject(new Key(0)), categoryA.getObject(new Key(2))));

        MappingBuilder builderA = new MappingBuilder();
        List<AccessPath> subpathsA = new ArrayList<>();

        subpathsA.add(builderA.simple("name", Signature.createBase(1)));
        subpathsA.add(builderA.simple("reviews", Signature.createBase(2)));

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

        ReferenceMerge edit = (new ReferenceMerge.Data(new Key(2), new Key(4))).createAlgorithm();

        SchemaCategory categoryFinal = edit.applySchemaCategoryEdit(category);

        System.out.println("Schema Category after edit:");
        System.out.println("Objects: " + categoryFinal.allObjects());
        for (SchemaMorphism m : categoryFinal.allMorphisms()) {
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature());
        }

        List<Mapping> editMappings = edit.applyMappingEdit(mappings);

        System.out.println(editMappings.get(0).accessPath());
    }

    @Test
    void testPrimaryKeyMergeEdit() {
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

        PrimaryKeyMerge edit = (new PrimaryKeyMerge.Data(new Key(1))).createAlgorithm();

        SchemaCategory categoryFinal = edit.applySchemaCategoryEdit(category);

        List<Mapping> editMappings = edit.applyMappingEdit(mappings);
/*
        System.out.println();
        System.out.println("Editted Size: ");
        System.out.println(editMappings.size());*/
    }

    @Test
    void testClusterEdit() {

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

        //List<Mapping> mappings = new ArrayList<>();
        //mappings.add(mapping);
        List<Key> clusterKeys = new ArrayList<>();
        clusterKeys.add(new Key(2));
        clusterKeys.add(new Key(6));

        ClusterMerge edit = (new ClusterMerge.Data(clusterKeys)).createAlgorithm();

        System.out.println("Schema Category before edit:");
        System.out.println("Objects: " + category.allObjects());
        for (SchemaMorphism m : category.allMorphisms()) {
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature());
        }
        System.out.println();

        SchemaCategory categoryFinal = edit.applySchemaCategoryEdit(category);

        System.out.println("Schema Category after edit:");
        System.out.println("Objects: " + categoryFinal.allObjects());
        for (SchemaMorphism m : categoryFinal.allMorphisms()) {
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature());
        }

        //List<Mapping> editMappings = edit.applyMappingEdit(mappings, categoryFinal);
/*
        System.out.println();
        System.out.println("Editted Size: ");
        System.out.println(editMappings.size());*/
    }

    @Test
    void testRecursionEdit() {

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
        //List<Mapping> mappings = new ArrayList<>();
        //mappings.add(mapping);
        List<PatternSegment> pattern = new ArrayList<>();
        pattern.add(new PatternSegment("A", "->"));
        pattern.add(new PatternSegment("B", "->"));
        pattern.add(new PatternSegment("A", ""));

        RecursionMerge edit = (new RecursionMerge.Data(pattern)).createAlgorithm();

        System.out.println("Schema Category before edit:");
        System.out.println("Objects: " + category.allObjects());
        for (SchemaMorphism m : category.allMorphisms()) {
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature());
        }
        System.out.println();

        SchemaCategory categoryFinal = edit.applySchemaCategoryEdit(category);

        System.out.println("Schema Category after edit:");
        System.out.println("Objects: " + categoryFinal.allObjects());
        for (SchemaMorphism m : categoryFinal.allMorphisms()) {
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature());
        }

        //List<Mapping> editMappings = edit.applyMappingEdit(mappings, categoryFinal);
/*
        System.out.println();
        System.out.println("Editted Size: ");
        System.out.println(editMappings.size());*/
    }

    @Test
    void testRecursionEdit2() {
        /**
         * With simple pattern A->B->A and notebook example
         */
        SchemaCategory category = new SchemaCategory("schema");
        category.addObject(new SchemaObject(new Key(0), "_id", null, null));
        category.addObject(new SchemaObject(new Key(1), "A", null, null));
        category.addObject(new SchemaObject(new Key(2), "B", null, null));
        category.addObject(new SchemaObject(new Key(3), "B", null, null));
        category.addObject(new SchemaObject(new Key(4), "A", null, null));
        category.addObject(new SchemaObject(new Key(5), "A", null, null));
        category.addObject(new SchemaObject(new Key(6), "A", null, null));
        category.addObject(new SchemaObject(new Key(7), "B", null, null));
        category.addObject(new SchemaObject(new Key(8), "B", null, null));
        category.addObject(new SchemaObject(new Key(9), "A", null, null));
        category.addObject(new SchemaObject(new Key(10), "B", null, null));
        category.addObject(new SchemaObject(new Key(11), "A", null, null));

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

        List<PatternSegment> pattern = new ArrayList<>();
        pattern.add(new PatternSegment("A", "->"));
        pattern.add(new PatternSegment("B", "->"));
        pattern.add(new PatternSegment("A", ""));

        RecursionMerge edit = (new RecursionMerge.Data(pattern)).createAlgorithm();

        System.out.println("Schema Category before edit:");
        System.out.println("Objects: " + category.allObjects());
        for (SchemaMorphism m : category.allMorphisms()) {
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature());
        }
        System.out.println();

        SchemaCategory categoryFinal = edit.applySchemaCategoryEdit(category);

        System.out.println("Schema Category after edit:");
        System.out.println("Objects: " + categoryFinal.allObjects());
        for (SchemaMorphism m : categoryFinal.allMorphisms()) {
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature());
        }
    }
    @Test
    void testRecursionEdit3() {
        /*
         * With complex pattern A<-A->B->A and notebook example short version
         */
        SchemaCategory category = new SchemaCategory("schema");
        category.addObject(new SchemaObject(new Key(0), "_id", null, null));
        category.addObject(new SchemaObject(new Key(1), "A", null, null));
        category.addObject(new SchemaObject(new Key(3), "B", null, null));
        category.addObject(new SchemaObject(new Key(4), "A", null, null));
        category.addObject(new SchemaObject(new Key(6), "A", null, null));
        category.addObject(new SchemaObject(new Key(8), "B", null, null));

        category.addMorphism(new SchemaMorphism(Signature.createBase(1), null, null,  new HashSet<>(), category.getObject(new Key(0)), category.getObject(new Key(1))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(3), null, null,  new HashSet<>(), category.getObject(new Key(1)), category.getObject(new Key(3))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(4), null, null,  new HashSet<>(), category.getObject(new Key(1)), category.getObject(new Key(4))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(6), null, null,  new HashSet<>(), category.getObject(new Key(3)), category.getObject(new Key(6))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(8), null, null,  new HashSet<>(), category.getObject(new Key(6)), category.getObject(new Key(8))));

        List<PatternSegment> pattern = new ArrayList<>();
        pattern.add(new PatternSegment("A", "<-"));
        pattern.add(new PatternSegment("A", "->"));
        pattern.add(new PatternSegment("B", "->"));
        pattern.add(new PatternSegment("A", ""));

        RecursionMerge edit = (new RecursionMerge.Data(pattern)).createAlgorithm();

        System.out.println("Schema Category before edit:");
        System.out.println("Objects: " + category.allObjects());
        for (SchemaMorphism m : category.allMorphisms()) {
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature());
        }
        System.out.println();

        SchemaCategory categoryFinal = edit.applySchemaCategoryEdit(category);

        System.out.println("Schema Category after edit:");
        System.out.println("Objects: " + categoryFinal.allObjects());
        for (SchemaMorphism m : categoryFinal.allMorphisms()) {
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature());
        }

    }
    @Test
    void testRecursionEdit4() {
        /*
         * With complex pattern A<-A->B->A and notebook example long version
         */
        SchemaCategory category = new SchemaCategory("schema");
        category.addObject(new SchemaObject(new Key(0), "_id", null, null));
        category.addObject(new SchemaObject(new Key(1), "A", null, null));
        category.addObject(new SchemaObject(new Key(2), "B", null, null));
        category.addObject(new SchemaObject(new Key(3), "B", null, null));
        category.addObject(new SchemaObject(new Key(4), "A", null, null));
        category.addObject(new SchemaObject(new Key(5), "A", null, null));
        category.addObject(new SchemaObject(new Key(6), "A", null, null));
        category.addObject(new SchemaObject(new Key(7), "B", null, null));
        category.addObject(new SchemaObject(new Key(8), "B", null, null));
        category.addObject(new SchemaObject(new Key(9), "A", null, null));
        category.addObject(new SchemaObject(new Key(10), "B", null, null));
        category.addObject(new SchemaObject(new Key(11), "A", null, null));

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

        List<PatternSegment> pattern = new ArrayList<>();
        pattern.add(new PatternSegment("A", "<-"));
        pattern.add(new PatternSegment("A", "->"));
        pattern.add(new PatternSegment("B", "->"));
        pattern.add(new PatternSegment("A", ""));

        RecursionMerge edit = (new RecursionMerge.Data(pattern)).createAlgorithm();

        System.out.println("Schema Category before edit:");
        System.out.println("Objects: " + category.allObjects());
        for (SchemaMorphism m : category.allMorphisms()) {
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature());
        }
        System.out.println();

        SchemaCategory categoryFinal = edit.applySchemaCategoryEdit(category);

        System.out.println("Schema Category after edit:");
        System.out.println("Objects: " + categoryFinal.allObjects());
        for (SchemaMorphism m : categoryFinal.allMorphisms()) {
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature());
        }

    }
    @Test
    void testRecursionEdit5() {
        /*
         * With complex pattern A<-A->B->A and client example
         */
        SchemaCategory category = new SchemaCategory("schema");
        category.addObject(new SchemaObject(new Key(0), "checkin", null, null));
        category.addObject(new SchemaObject(new Key(1), "_id", null, null));
        category.addObject(new SchemaObject(new Key(2), "A", null, null));
        category.addObject(new SchemaObject(new Key(3), "A", null, null));
        category.addObject(new SchemaObject(new Key(4), "B", null, null));
        category.addObject(new SchemaObject(new Key(5), "A", null, null));
        category.addObject(new SchemaObject(new Key(6), "B", null, null));
        category.addObject(new SchemaObject(new Key(7), "A", null, null));
        category.addObject(new SchemaObject(new Key(8), "B", null, null));
        category.addObject(new SchemaObject(new Key(9), "A", null, null));
        category.addObject(new SchemaObject(new Key(10), "B", null, null));
        category.addObject(new SchemaObject(new Key(11), "B", null, null));
        category.addObject(new SchemaObject(new Key(12), "A", null, null));
        category.addObject(new SchemaObject(new Key(13), "business_id", null, null));
        category.addObject(new SchemaObject(new Key(14), "date", null, null));
        category.addObject(new SchemaObject(new Key(15), "date", null, null));


        category.addMorphism(new SchemaMorphism(Signature.createBase(0), null, null,  new HashSet<>(), category.getObject(new Key(0)), category.getObject(new Key(1))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(12), null, null,  new HashSet<>(), category.getObject(new Key(0)), category.getObject(new Key(13))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(13), null, null,  new HashSet<>(), category.getObject(new Key(0)), category.getObject(new Key(14))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(14), null, null,  new HashSet<>(), category.getObject(new Key(0)), category.getObject(new Key(15))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(10), null, null,  new HashSet<>(), category.getObject(new Key(1)), category.getObject(new Key(11))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(1), null, null,  new HashSet<>(), category.getObject(new Key(1)), category.getObject(new Key(2))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(11), null, null,  new HashSet<>(), category.getObject(new Key(11)), category.getObject(new Key(12))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(7), null, null,  new HashSet<>(), category.getObject(new Key(2)), category.getObject(new Key(8))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(2), null, null,  new HashSet<>(), category.getObject(new Key(2)), category.getObject(new Key(3))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(3), null, null,  new HashSet<>(), category.getObject(new Key(3)), category.getObject(new Key(4))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(4), null, null,  new HashSet<>(), category.getObject(new Key(4)), category.getObject(new Key(5))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(5), null, null,  new HashSet<>(), category.getObject(new Key(5)), category.getObject(new Key(6))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(6), null, null,  new HashSet<>(), category.getObject(new Key(6)), category.getObject(new Key(7))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(8), null, null,  new HashSet<>(), category.getObject(new Key(8)), category.getObject(new Key(9))));
        category.addMorphism(new SchemaMorphism(Signature.createBase(9), null, null,  new HashSet<>(), category.getObject(new Key(9)), category.getObject(new Key(10))));

        List<PatternSegment> pattern = new ArrayList<>();
        pattern.add(new PatternSegment("A", "<-"));
        pattern.add(new PatternSegment("A", "->"));
        pattern.add(new PatternSegment("B", "->"));
        pattern.add(new PatternSegment("A", ""));

        RecursionMerge edit = (new RecursionMerge.Data(pattern)).createAlgorithm();

        System.out.println("Schema Category before edit:");
        System.out.println("Objects: " + category.allObjects());
        for (SchemaMorphism m : category.allMorphisms()) {
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature());
        }
        System.out.println();

        SchemaCategory categoryFinal = edit.applySchemaCategoryEdit(category);

        System.out.println("Schema Category after edit:");
        System.out.println("Objects: " + categoryFinal.allObjects());
        for (SchemaMorphism m : categoryFinal.allMorphisms()) {
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature());
        }

    }

}
