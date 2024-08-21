package cz.matfyz.tests.inference;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.MappingBuilder;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.edit.InferenceEditAlgorithm;
import cz.matfyz.inference.edit.PatternSegment;
import cz.matfyz.inference.edit.algorithms.ClusterMerge;
import cz.matfyz.inference.edit.algorithms.PrimaryKeyMerge;
import cz.matfyz.inference.edit.algorithms.RecursionMerge;
import cz.matfyz.inference.edit.algorithms.ReferenceMerge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class SchemaCategoryInferenceEditorTests {

    @Test
    void testReferenceMergeEditArray() {
        final SchemaBuilder sbA = new SchemaBuilder();
        final var app =             sbA.object("app", 0);
        final var name =            sbA.object("name", 1);
        final var reviewsA =        sbA.object("reviews", 2);
        final var index =           sbA.object("_index", 3);
        final var appToName =       sbA.morphism(app, name, 1);
        final var reviewsAToApp =   sbA.morphism(reviewsA, app, 2);
        final var reviewsAToIndex = sbA.morphism(reviewsA, index, 3);
        final SchemaCategory schemaA = sbA.build();

        final MappingBuilder mbA = new MappingBuilder();
        final Mapping mappingA = new Mapping(
            schemaA,
            name.key(),
            "kindNameA",
            mbA.root(
                mbA.simple("name", appToName),
                mbA.complex("reviews", reviewsAToApp.dual(),
                    mbA.simple("_index", reviewsAToIndex)
                )
            ),
            null
        );

        System.out.println(mappingA.accessPath());
        System.out.println();

        // Setup B
        final SchemaBuilder sbB = new SchemaBuilder();
        final var reviewsB =        sbB.object("reviews", 4);
        final var text =            sbB.object("text", 5);
        final var reviewsBToText =  sbB.morphism(reviewsB, text, 5);
        final SchemaCategory schemaB = sbB.build();

        final MappingBuilder mbB = new MappingBuilder();
        final Mapping mappingB = new Mapping(
            schemaB,
            reviewsB.key(),
            "kindNameB",
            mbB.root(
                mbB.simple("text", reviewsBToText)
            ),
            null
        );

        System.out.println(mappingB.accessPath());

        final SchemaCategory schema = mergeSchemas(schemaA, schemaB);
        final MetadataCategory metadata = mergeMetadatas(schema, sbA.buildMetadata(schemaA), sbB.buildMetadata(schemaB));
        final List<Mapping> mappings = List.of(mappingA, mappingB);

        final ReferenceMerge edit = (new ReferenceMerge.Data(0, true, reviewsA.key(), reviewsB.key(), null)).createAlgorithm();
        testAlgorithm(schema, metadata, edit);

        final List<Mapping> editMappings = edit.applyMappingEdit(mappings);

        System.out.println(editMappings.get(0).accessPath());
    }

    @Test
    void testReferenceMergeEditObject() {
        // Setup A
        final SchemaBuilder sbA = new SchemaBuilder();
        final var app =             sbA.object("app", 0);
        final var name =            sbA.object("name", 1);
        final var reviewsA =        sbA.object("reviews", 2);
        final var appToName =       sbA.morphism(app, name, 1);
        final var appToReviewsA =   sbA.morphism(app, reviewsA, 2);
        final SchemaCategory schemaA = sbA.build();

        final MappingBuilder mbA = new MappingBuilder();
        final Mapping mappingA = new Mapping(
            schemaA,
            name.key(),
            "kindNameA",
            mbA.root(
                mbA.simple("name", appToName),
                mbA.simple("reviews", appToReviewsA)
            ),
            null
        );

        System.out.println(mappingA.accessPath());
        System.out.println();

        // Setup B
        final SchemaBuilder sbB = new SchemaBuilder();
        final var reviewsB =        sbB.object("reviews", 4);
        final var text =            sbB.object("text", 5);
        final var reviewsBToText =  sbB.morphism(reviewsB, text, 5);
        final SchemaCategory schemaB = sbB.build();

        final MappingBuilder mbB = new MappingBuilder();
        final Mapping mappingB = new Mapping(
            schemaB,
            reviewsB.key(),
            "kindNameB",
            mbB.root(
                mbB.simple("text", reviewsBToText)
            ),
            null
        );

        System.out.println(mappingB.accessPath());

        final SchemaCategory schema = mergeSchemas(schemaA, schemaB);
        final MetadataCategory metadata = mergeMetadatas(schema, sbA.buildMetadata(schemaA), sbB.buildMetadata(schemaB));
        final List<Mapping> mappings = List.of(mappingA, mappingB);

        final ReferenceMerge edit = (new ReferenceMerge.Data(0, true, reviewsA.key(), reviewsB.key(), null)).createAlgorithm();
        testAlgorithm(schema, metadata, edit);

        final List<Mapping> editMappings = edit.applyMappingEdit(mappings);

        System.out.println(editMappings.get(0).accessPath());
    }

    @Test
    void testPrimaryKeyMergeEdit() {
        // Setup A
        final SchemaBuilder sbA = new SchemaBuilder();
        final var app =         sbA.object("app", 0);
        final var appIdA =      sbA.object("app_id", 1);
        final var name =        sbA.object("name", 2);
        final var appToAppIdA = sbA.morphism(app, appIdA, 1);
        final var appToName =   sbA.morphism(app, name, 2);
        final SchemaCategory schemaA = sbA.build();

        final MappingBuilder mbA = new MappingBuilder();
        final Mapping mappingA = new Mapping(
            schemaA,
            app.key(),
            "kindNameA",
            mbA.root(
                mbA.simple("name", appToAppIdA),
                mbA.simple("app_id", appToName)
            ),
            null
        );

        System.out.println(mappingA.accessPath());
        System.out.println();

        // Setup B
        final SchemaBuilder sbB = new SchemaBuilder();
        final var reviews =         sbB.object("reviews", 3);
        final var appIdB =          sbB.object("app_id", 4);
        final var text =            sbB.object("text", 5);
        final var reviewsToAppIdB = sbB.morphism(reviews, appIdB, 3);
        final var reviewsToText =   sbB.morphism(reviews, text, 4);
        final SchemaCategory schemaB = sbB.build();

        final MappingBuilder mbB = new MappingBuilder();
        final Mapping mappingB = new Mapping(
            schemaB,
            reviews.key(),
            "kindNameB",
            mbB.root(
                mbB.simple("text", reviewsToText),
                mbB.simple("app_id", reviewsToAppIdB)
            ),
            null
        );

        System.out.println(mappingB.accessPath());

        final SchemaCategory schema = mergeSchemas(schemaA, schemaB);
        final MetadataCategory metadata = mergeMetadatas(schema, sbA.buildMetadata(schemaA), sbB.buildMetadata(schemaB));
        final List<Mapping> mappings = List.of(mappingA, mappingB);

        final PrimaryKeyMerge edit = (new PrimaryKeyMerge.Data(0, true, appIdA.key(), null)).createAlgorithm();
        testAlgorithm(schema, metadata, edit);

        final List<Mapping> editMappings = edit.applyMappingEdit(mappings);
    }

    @Test
    void testClusterEdit() {
        final var builder = new SchemaBuilder();

        final var world =       builder.object("world", 0);
        final var continent =   builder.object("continent", 1);
        final var country1 =    builder.object("country_1", 2);
        final var o3 =          builder.object("a", 3);
        final var o4 =          builder.object("b", 4);
        final var o5 =          builder.object("c", 5);
        final var country2 =    builder.object("country_2", 6);
        final var o7 =          builder.object("a", 7);
        final var o8 =          builder.object("b", 8);
        final var o9 =          builder.object("c", 9);

        final var worldToContinent =        builder.morphism(world, continent, 1);
        final var continentToCountry1 =     builder.morphism(continent, country1, 2);
        final var continentToCountry2 =     builder.morphism(continent, country2, 3);
        final var country1ToA =             builder.morphism(country1, o3, 4);
        final var country1ToB =             builder.morphism(country1, o4, 5);
        final var aToC1 =                   builder.morphism(o3, o5, 6);
        final var country2ToA =             builder.morphism(country2, o7, 7);
        final var country2ToB =             builder.morphism(country2, o8, 8);
        final var aToC2 =                   builder.morphism(o7, o9, 9);

        final SchemaCategory schema = builder.build();
        final MetadataCategory metadata = builder.buildMetadata(schema);

        final MappingBuilder mb = new MappingBuilder();
        final Mapping mapping = new Mapping(
            schema,
            world.key(),
            "kindName",
            mb.root(
                mb.complex("continent", worldToContinent,
                    mb.complex("country_1", continentToCountry1,
                        mb.complex("a", country1ToA,
                            mb.simple("c", aToC1)),
                        mb.simple("b", country1ToB)),
                    mb.complex("country_2", continentToCountry2,
                        mb.complex("a", country2ToA,
                            mb.simple("c", aToC2)),
                        mb.simple("b", country2ToB)
                            )
                        )
            ),
            null
        );

        System.out.println("Mapping before edit:");
        System.out.println(mapping.accessPath());


        List<Key> clusterKeys = new ArrayList<>();
        clusterKeys.add(country1.key());
        clusterKeys.add(country2.key());

        final ClusterMerge edit = (new ClusterMerge.Data(0, true, clusterKeys)).createAlgorithm();
        testAlgorithm(schema, metadata, edit);

        final List<Mapping> editMappings = edit.applyMappingEdit(List.of(mapping));

        System.out.println("Mapping after edit:");
        System.out.println(editMappings.get(0).accessPath());
    }

    @Test
    void testRecursionEdit() {
        final var builder = new SchemaBuilder();

        final var o0 =  builder.object("B", 0);
        final var o1 =  builder.object("A", 1);
        final var o2 =  builder.object("A", 2);
        final var o3 =  builder.object("B", 3);
        final var o4 =  builder.object("B", 4);
        final var o5 =  builder.object("B", 5);
        final var o6 =  builder.object("A", 6);
        final var o7 =  builder.object("A", 7);
        final var o8 =  builder.object("B", 8);
        final var o9 =  builder.object("B", 9);
        final var o10 = builder.object("A", 10);
        final var o11 = builder.object("B", 11);

        builder.morphism(o0, o1, 1);
        builder.morphism(o0, o2, 2);
        builder.morphism(o1, o3, 3);
        builder.morphism(o1, o4, 4);
        builder.morphism(o2, o5, 5);
        builder.morphism(o3, o6, 6);
        builder.morphism(o4, o7, 7);
        builder.morphism(o6, o8, 8);
        builder.morphism(o7, o9, 9);
        builder.morphism(o9, o10, 10);
        builder.morphism(o10, o11, 11);

        final SchemaCategory schema = builder.build();
        final MetadataCategory metadata = builder.buildMetadata(schema);

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

        final RecursionMerge edit = (new RecursionMerge.Data(0, true, pattern)).createAlgorithm();
        testAlgorithm(schema, metadata, edit);

        //List<Mapping> editMappings = edit.applyMappingEdit(mappings, categoryFinal);
/*
        System.out.println();
        System.out.println("Editted Size: ");
        System.out.println(editMappings.size());*/
    }

    /**
     * With simple pattern A->B->A and notebook example
     */
    @Test
    void testRecursionEdit2() {
        final var builder = new SchemaBuilder();

        final var id =  builder.object("_id", 0);
        final var o1 =  builder.object("A", 1);
        final var o2 =  builder.object("B", 2);
        final var o3 =  builder.object("B", 3);
        final var o4 =  builder.object("A", 4);
        final var o5 =  builder.object("A", 5);
        final var o6 =  builder.object("A", 6);
        final var o7 =  builder.object("B", 7);
        final var o8 =  builder.object("B", 8);
        final var o9 =  builder.object("A", 9);
        final var o10 = builder.object("B", 10);
        final var o11 = builder.object("A", 11);

        builder.morphism(id, o1, 1);
        builder.morphism(id, o2, 2);
        builder.morphism(o1, o3, 3);
        builder.morphism(o1, o4, 4);
        builder.morphism(o2, o5, 5);
        builder.morphism(o3, o6, 6);
        builder.morphism(o4, o7, 7);
        builder.morphism(o6, o8, 8);
        builder.morphism(o7, o9, 9);
        builder.morphism(o9, o10, 10);
        builder.morphism(o10, o11, 11);

        final SchemaCategory schema = builder.build();
        final MetadataCategory metadata = builder.buildMetadata(schema);

        List<PatternSegment> pattern = new ArrayList<>();
        pattern.add(new PatternSegment("A", "->"));
        pattern.add(new PatternSegment("B", "->"));
        pattern.add(new PatternSegment("A", ""));

        final RecursionMerge edit = (new RecursionMerge.Data(0, true, pattern)).createAlgorithm();
        testAlgorithm(schema, metadata, edit);
    }

    /**
     * With complex pattern A<-A->B->A and notebook example short version
     */
    @Test
    void testRecursionEdit3() {
        final var builder = new SchemaBuilder();

        final var id =  builder.object("_id", 0);
        final var o1 =  builder.object("A", 1);
        final var o3 =  builder.object("B", 3);
        final var o4 =  builder.object("A", 4);
        final var o6 =  builder.object("A", 6);
        final var o8 =  builder.object("B", 8);

        builder.morphism(id, o1, 1);
        builder.morphism(o1, o3, 3);
        builder.morphism(o1, o4, 4);
        builder.morphism(o3, o6, 6);
        builder.morphism(o6, o8, 8);

        final SchemaCategory schema = builder.build();
        final MetadataCategory metadata = builder.buildMetadata(schema);

        List<PatternSegment> pattern = new ArrayList<>();
        pattern.add(new PatternSegment("A", "<-"));
        pattern.add(new PatternSegment("A", "->"));
        pattern.add(new PatternSegment("B", "->"));
        pattern.add(new PatternSegment("A", ""));

        final RecursionMerge edit = (new RecursionMerge.Data(0, true, pattern)).createAlgorithm();
        testAlgorithm(schema, metadata, edit);
    }

    /**
     * With complex pattern A<-A->B->A and notebook example long version
     */
    @Test
    void testRecursionEdit4() {
        final var builder = new SchemaBuilder();

        final var id =  builder.object("_id", 0);
        final var o1 =  builder.object("A", 1);
        final var o2 =  builder.object("B", 2);
        final var o3 =  builder.object("B", 3);
        final var o4 =  builder.object("A", 4);
        final var o5 =  builder.object("A", 5);
        final var o6 =  builder.object("A", 6);
        final var o7 =  builder.object("B", 7);
        final var o8 =  builder.object("B", 8);
        final var o9 =  builder.object("A", 9);
        final var o10 = builder.object("B", 10);
        final var o11 = builder.object("A", 11);

        builder.morphism(id, o1, 1);
        builder.morphism(id, o2, 2);
        builder.morphism(o1, o3, 3);
        builder.morphism(o1, o4, 4);
        builder.morphism(o2, o5, 5);
        builder.morphism(o3, o6, 6);
        builder.morphism(o4, o7, 7);
        builder.morphism(o6, o8, 8);
        builder.morphism(o7, o9, 9);
        builder.morphism(o9, o10, 10);
        builder.morphism(o10, o11, 11);

        final SchemaCategory schema = builder.build();
        final MetadataCategory metadata = builder.buildMetadata(schema);

        List<PatternSegment> pattern = new ArrayList<>();
        pattern.add(new PatternSegment("A", "<-"));
        pattern.add(new PatternSegment("A", "->"));
        pattern.add(new PatternSegment("B", "->"));
        pattern.add(new PatternSegment("A", ""));

        final RecursionMerge edit = (new RecursionMerge.Data(0, true, pattern)).createAlgorithm();
        testAlgorithm(schema, metadata, edit);
    }

    /**
     * With complex pattern A<-A->B->A and client example
     */
    @Test
    void testRecursionEdit5() {
        System.out.println("in test 5");
        final var builder = new SchemaBuilder();

        final var checkin =     builder.object("checkin", 0);
        final var id =          builder.object("_id", 1);
        final var o2 =          builder.object("A", 2);
        final var o3 =          builder.object("A", 3);
        final var o4 =          builder.object("B", 4);
        final var o5 =          builder.object("A", 5);
        //final var o6 =          builder.object("B", 6);
        //final var o7 =          builder.object("A", 7);
        final var o8 =          builder.object("B", 8);
        final var o9 =          builder.object("A", 9);
        final var o10 =         builder.object("B", 10);
        final var o11 =         builder.object("B", 11);
        final var o12 =         builder.object("A", 12);
        final var businessId =  builder.object("business_id", 13);
        final var date1 =       builder.object("date", 14);
        final var date2 =       builder.object("date", 15);

        builder.morphism(checkin, id, 0);
        builder.morphism(checkin, businessId, 12);
        builder.morphism(checkin, date1, 13);
        builder.morphism(checkin, date2, 14);
        builder.morphism(id, o11, 10);
        builder.morphism(id, o2, 1);
        builder.morphism(o11, o12, 11);
        builder.morphism(o2, o8, 7);
        builder.morphism(o2, o3, 2);
        builder.morphism(o3, o4, 3);
        builder.morphism(o4, o5, 4);
        //builder.morphism(o5, o6, 5);
        //builder.morphism(o6, o7, 6);
        builder.morphism(o8, o9, 8);
        builder.morphism(o9, o10, 9);

        final SchemaCategory schema = builder.build();
        final MetadataCategory metadata = builder.buildMetadata(schema);

        List<PatternSegment> pattern = new ArrayList<>();
        /*
        pattern.add(new PatternSegment("A", "<-"));
        pattern.add(new PatternSegment("A", "->"));
        pattern.add(new PatternSegment("B", "->"));
        pattern.add(new PatternSegment("A", ""));
         */

        pattern.add(new PatternSegment("A", "->"));
        pattern.add(new PatternSegment("B", "->"));
        pattern.add(new PatternSegment("A", ""));

        final RecursionMerge edit = (new RecursionMerge.Data(0, true, pattern)).createAlgorithm();
        testAlgorithm(schema, metadata, edit);
    }

    private SchemaCategory mergeSchemas(SchemaCategory... schemas) {
        final SchemaCategory output = new SchemaCategory();
        for (final SchemaCategory schema : schemas) {
            for (final SchemaObject object : schema.allObjects())
                schema.addObject(object);

            for (final SchemaMorphism morphism : schema.allMorphisms())
                schema.addMorphism(morphism);
        }

        return output;
    }

    private MetadataCategory mergeMetadatas(SchemaCategory schema, MetadataCategory... metadatas) {
        final MetadataCategory output = MetadataCategory.createEmpty(schema);

        for (final SchemaObject object : schema.allObjects()) {
            for (final MetadataCategory metadata : metadatas) {
                final var mo = metadata.getObject(object);
                if (mo != null) {
                    output.setObject(object, mo);
                    break;
                }
            }
        }

        for (final SchemaMorphism morphism : schema.allMorphisms()) {
            for (final MetadataCategory metadata : metadatas) {
                final var mm = metadata.getMorphism(morphism);
                if (mm != null) {
                    output.setMorphism(morphism, mm);
                    break;
                }
            }
        }

        return output;
    }

    private void testAlgorithm(SchemaCategory schema, MetadataCategory metadata, InferenceEditAlgorithm edit) {
        System.out.println("Schema Category before edit:");
        printCategory(schema, metadata);
        System.out.println();

        final var result = edit.applyCategoryEdit(schema, metadata);

        System.out.println("Schema category after edit:");
        printCategory(result.schema(), result.metadata());

        System.out.println("Metadata category after edit:");
        // TODO
    }

    private void printCategory(SchemaCategory schema, MetadataCategory metadata) {
        System.out.println("Objects: ");
        for (SchemaObject o : schema.allObjects()) {
            final var mo = metadata.getObject(o);
            System.out.println("Object: " + o + " label: " + mo.label);
        }

        for (SchemaMorphism m : schema.allMorphisms()) {
            final var mm = metadata.getMorphism(m);
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature() + " label: " + mm.label);
        }
    }

}
