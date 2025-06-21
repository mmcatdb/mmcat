package cz.matfyz.tests.inference;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.AccessPathBuilder;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.inference.edit.InferenceEditAlgorithm;
import cz.matfyz.inference.edit.PatternSegment;
import cz.matfyz.inference.edit.algorithms.ClusterMerge;
import cz.matfyz.inference.edit.algorithms.PrimaryKeyMerge;
import cz.matfyz.inference.edit.algorithms.RecursionMerge;
import cz.matfyz.inference.edit.algorithms.ReferenceMerge;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class SchemaCategoryInferenceEditorTests {

    // The type literally doesn't matter.
    private static final Datasource datasource = new Datasource(DatasourceType.mongodb, "test");

    @Test
    void testReferenceMergeEditArray() {
        final SchemaBuilder sbA = new SchemaBuilder();
        final var app =             sbA.objex("app", 0);
        final var name =            sbA.objex("name", 1);
        final var reviewsA =        sbA.objex("reviews", 2);
        final var index =           sbA.objex("_index", 3);
        final var appToName =       sbA.morphism(app, name, 1);
        final var reviewsAToApp =   sbA.morphism(reviewsA, app, 2);
        final var reviewsAToIndex = sbA.morphism(reviewsA, index, 3);
        final SchemaCategory schemaA = sbA.build();

        final AccessPathBuilder apbA = new AccessPathBuilder();
        final Mapping mappingA = Mapping.create(
            datasource,
            "kindNameA",
            schemaA,
            name.key(),
            apbA.root(
                apbA.simple("name", appToName),
                apbA.complex("reviews", reviewsAToApp.dual(),
                    apbA.simple("_index", reviewsAToIndex)
                )
            )
        );

        System.out.println(mappingA.accessPath());
        System.out.println();

        // Setup B
        final SchemaBuilder sbB = new SchemaBuilder();
        final var reviewsB =        sbB.objex("reviews", 4);
        final var text =            sbB.objex("text", 5);
        final var reviewsBToText =  sbB.morphism(reviewsB, text, 5);
        final SchemaCategory schemaB = sbB.build();

        final AccessPathBuilder apbB = new AccessPathBuilder();
        final Mapping mappingB = Mapping.create(
            datasource,
            "kindNameB",
            schemaB,
            reviewsB.key(),
            apbB.root(
                apbB.simple("text", reviewsBToText)
            )
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
    void testReferenceMergeEditObjex() {
        // Setup A
        final SchemaBuilder sbA = new SchemaBuilder();
        final var app =             sbA.objex("app", 0);
        final var name =            sbA.objex("name", 1);
        final var reviewsA =        sbA.objex("reviews", 2);
        final var appToName =       sbA.morphism(app, name, 1);
        final var appToReviewsA =   sbA.morphism(app, reviewsA, 2);
        final SchemaCategory schemaA = sbA.build();

        final AccessPathBuilder apbA = new AccessPathBuilder();
        final Mapping mappingA = Mapping.create(
            datasource,
            "kindNameA",
            schemaA,
            name.key(),
            apbA.root(
                apbA.simple("name", appToName),
                apbA.simple("reviews", appToReviewsA)
            )
        );

        System.out.println(mappingA.accessPath());
        System.out.println();

        // Setup B
        final SchemaBuilder sbB = new SchemaBuilder();
        final var reviewsB =        sbB.objex("reviews", 4);
        final var text =            sbB.objex("text", 5);
        final var reviewsBToText =  sbB.morphism(reviewsB, text, 5);
        final SchemaCategory schemaB = sbB.build();

        final AccessPathBuilder apbB = new AccessPathBuilder();
        final Mapping mappingB = Mapping.create(
            datasource,
            "kindNameB",
            schemaB,
            reviewsB.key(),
            apbB.root(
                apbB.simple("text", reviewsBToText)
            )
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
        final var app =         sbA.objex("app", 0);
        final var appIdA =      sbA.objex("app_id", 1);
        final var name =        sbA.objex("name", 2);
        final var appToAppIdA = sbA.morphism(app, appIdA, 1);
        final var appToName =   sbA.morphism(app, name, 2);
        final SchemaCategory schemaA = sbA.build();

        final AccessPathBuilder apbA = new AccessPathBuilder();
        final Mapping mappingA = Mapping.create(
            datasource,
            "kindNameA",
            schemaA,
            app.key(),
            apbA.root(
                apbA.simple("name", appToAppIdA),
                apbA.simple("app_id", appToName)
            )
        );

        System.out.println(mappingA.accessPath());
        System.out.println();

        // Setup B
        final SchemaBuilder sbB = new SchemaBuilder();
        final var reviews =         sbB.objex("reviews", 3);
        final var appIdB =          sbB.objex("app_id", 4);
        final var text =            sbB.objex("text", 5);
        final var reviewsToAppIdB = sbB.morphism(reviews, appIdB, 3);
        final var reviewsToText =   sbB.morphism(reviews, text, 4);
        final SchemaCategory schemaB = sbB.build();

        final AccessPathBuilder apbB = new AccessPathBuilder();
        final Mapping mappingB = Mapping.create(
            datasource,
            "kindNameB",
            schemaB,
            reviews.key(),
            apbB.root(
                apbB.simple("text", reviewsToText),
                apbB.simple("app_id", reviewsToAppIdB)
            )
        );

        System.out.println(mappingB.accessPath());

        final SchemaCategory schema = mergeSchemas(schemaA, schemaB);
        final MetadataCategory metadata = mergeMetadatas(schema, sbA.buildMetadata(schemaA), sbB.buildMetadata(schemaB));
        final List<Mapping> mappings = List.of(mappingA, mappingB);

        final PrimaryKeyMerge edit = (new PrimaryKeyMerge.Data(0, true, appIdA.key(), reviews.key(), null)).createAlgorithm();
        testAlgorithm(schema, metadata, edit);

        final List<Mapping> editMappings = edit.applyMappingEdit(mappings);
    }

    @Test
    void testClusterComplexEdit() {
        final var builder = new SchemaBuilder();

        final var world =       builder.objex("world", 0);
        final var continent =   builder.objex("continent", 1);
        final var country1 =    builder.objex("country_1", 2);
        final var o3 =          builder.objex("a", 3);
        final var o4 =          builder.objex("b", 4);
        final var o5 =          builder.objex("c", 5);
        final var country2 =    builder.objex("country_2", 6);
        final var o7 =          builder.objex("a", 7);
        final var o8 =          builder.objex("b", 8);
        final var o9 =          builder.objex("c", 9);

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

        final AccessPathBuilder apb = new AccessPathBuilder();
        final Mapping mapping = Mapping.create(
            datasource,
            "kindName",
            schema,
            world.key(),
            apb.root(
                apb.complex("continent", worldToContinent,
                    apb.complex("country_1", continentToCountry1,
                        apb.complex("a", country1ToA,
                            apb.simple("c", aToC1)),
                        apb.simple("b", country1ToB)),
                    apb.complex("country_2", continentToCountry2,
                        apb.complex("a", country2ToA,
                            apb.simple("c", aToC2)),
                        apb.simple("b", country2ToB)
                            )
                        )
            )
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
    void testClusterIntermediateEdit() {
        final var builder = new SchemaBuilder();

        final var user =            builder.objex("user", 0);
        final var name =            builder.objex("name", 1);
        final var compliment1 =     builder.objex("compliment_cute", 2);
        final var rating1 =         builder.objex("rating", 3);
        final var comments1 =       builder.objex("comments", 4);
        final var compliment2 =     builder.objex("compliment_funny", 5);
        final var rating2 =         builder.objex("rating", 6);
        final var comments2 =       builder.objex("comments", 7);

        final var userToName =              builder.morphism(user, name, 1);
        final var userToCompliment1 =       builder.morphism(user, compliment1, 2);
        final var compliment1ToRating =     builder.morphism(compliment1, rating1, 3);
        final var compliment1ToComments =   builder.morphism(compliment1, comments1, 4);
        final var userToCompliment2 =       builder.morphism(user, compliment2, 5);
        final var compliment2ToRating =     builder.morphism(compliment2, rating2, 6);
        final var compliment2ToComments =   builder.morphism(compliment2, comments2, 7);

        final SchemaCategory schema = builder.build();
        final MetadataCategory metadata = builder.buildMetadata(schema);

        final AccessPathBuilder apb = new AccessPathBuilder();
        final Mapping mapping = Mapping.create(
            datasource,
            "kindName",
            schema,
            user.key(),
            apb.root(
                apb.simple("name", userToName),
                apb.complex("compliment_cute", userToCompliment1,
                    apb.simple("rating", compliment1ToRating),
                    apb.simple("comments", compliment1ToComments)),
                apb.complex("compliment_funny", userToCompliment2,
                    apb.simple("rating", compliment2ToRating),
                    apb.simple("comments", compliment2ToComments))
            )
        );

        System.out.println("Mapping before edit:");
        System.out.println(mapping.accessPath());


        List<Key> clusterKeys = new ArrayList<>();
        clusterKeys.add(compliment1.key());
        clusterKeys.add(compliment2.key());

        final ClusterMerge edit = (new ClusterMerge.Data(0, true, clusterKeys)).createAlgorithm();
        testAlgorithm(schema, metadata, edit);

        final List<Mapping> editMappings = edit.applyMappingEdit(List.of(mapping));

        System.out.println("Mapping after edit:");
        System.out.println(editMappings.get(0).accessPath());
    }

    @Test
    void testClusterSimpleEdit() {
        final var builder = new SchemaBuilder();

        final var user =            builder.objex("user", 0);
        final var name =            builder.objex("name", 1);
        final var compliment1 =     builder.objex("compliment_cute", 2);
        final var compliment2 =     builder.objex("compliment_funny", 3);

        final var userToName =              builder.morphism(user, name, 1);
        final var userToCompliment1 =       builder.morphism(user, compliment1, 2);
        final var userToCompliment2 =       builder.morphism(user, compliment2, 3);

        final SchemaCategory schema = builder.build();
        final MetadataCategory metadata = builder.buildMetadata(schema);

        final AccessPathBuilder apb = new AccessPathBuilder();
        final Mapping mapping = Mapping.create(
            datasource,
            "kindName",
            schema,
            user.key(),
            apb.root(
                apb.simple("name", userToName),
                apb.simple("compliment_cute", userToCompliment1),
                apb.simple("compliment_funny", userToCompliment2)
            )
        );

        System.out.println("Mapping before edit:");
        System.out.println(mapping.accessPath());


        List<Key> clusterKeys = new ArrayList<>();
        clusterKeys.add(compliment1.key());
        clusterKeys.add(compliment2.key());

        final ClusterMerge edit = (new ClusterMerge.Data(0, true, clusterKeys)).createAlgorithm();
        testAlgorithm(schema, metadata, edit);

        final List<Mapping> editMappings = edit.applyMappingEdit(List.of(mapping));

        System.out.println("Mapping after edit:");
        System.out.println(editMappings.get(0).accessPath());
    }

    @Test
    void testRecursionEdit() {
        final var builder = new SchemaBuilder();

        final var o0 =  builder.objex("B", 0);
        final var o1 =  builder.objex("A", 1);
        final var o2 =  builder.objex("A", 2);
        final var o3 =  builder.objex("B", 3);
        //final var o4 =  builder.objex("B", 4);
        final var o5 =  builder.objex("B", 5);
        final var o6 =  builder.objex("A", 6);
        //final var o7 =  builder.objex("A", 7);
        final var o8 =  builder.objex("B", 8);
        //final var o9 =  builder.objex("B", 9);
        //final var o10 = builder.objex("A", 10);
        //final var o11 = builder.objex("B", 11);

        builder.morphism(o0, o1, 1);
        builder.morphism(o0, o2, 2);
        builder.morphism(o1, o3, 3);
        //builder.morphism(o1, o4, 4);
        builder.morphism(o2, o5, 5);
        builder.morphism(o3, o6, 6);
        //builder.morphism(o4, o7, 7);
        builder.morphism(o6, o8, 8);
        //builder.morphism(o7, o9, 9);
        //builder.morphism(o9, o10, 10);
        //builder.morphism(o10, o11, 11);

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
     * With simple pattern A->B->A and notebook example
     */
    @Test
    void testRecursionEdit2() {
        final var builder = new SchemaBuilder();

        final var id =  builder.objex("_id", 0);
        final var o1 =  builder.objex("A", 1);
        final var o2 =  builder.objex("B", 2);
        final var o3 =  builder.objex("B", 3);
        final var o4 =  builder.objex("A", 4);
        final var o5 =  builder.objex("A", 5);
        final var o6 =  builder.objex("A", 6);
        final var o7 =  builder.objex("B", 7);
        final var o8 =  builder.objex("B", 8);
        final var o9 =  builder.objex("A", 9);
        final var o10 = builder.objex("B", 10);
        final var o11 = builder.objex("A", 11);

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

    private SchemaCategory mergeSchemas(SchemaCategory... schemas) {
        final SchemaCategory output = new SchemaCategory();
        for (final SchemaCategory schema : schemas) {
            for (final SchemaObjex objex : schema.allObjexes())
                schema.addObjex(objex);

            for (final SchemaMorphism morphism : schema.allMorphisms())
                schema.addMorphism(morphism);
        }

        return output;
    }

    private MetadataCategory mergeMetadatas(SchemaCategory schema, MetadataCategory... metadatas) {
        final MetadataCategory output = MetadataCategory.createEmpty(schema);

        for (final SchemaObjex objex : schema.allObjexes()) {
            for (final MetadataCategory metadata : metadatas) {
                final var mo = metadata.getObjex(objex);
                if (mo != null) {
                    output.setObjex(objex, mo);
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
        System.out.println("Objexes: ");
        for (SchemaObjex o : schema.allObjexes()) {
            final var mo = metadata.getObjex(o);
            System.out.println("Objex: " + o + " label: " + mo.label);
        }

        for (SchemaMorphism m : schema.allMorphisms()) {
            final var mm = metadata.getMorphism(m);
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature() + " label: " + mm.label);
        }
    }

}
