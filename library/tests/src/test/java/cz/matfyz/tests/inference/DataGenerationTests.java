package cz.matfyz.tests.inference;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceCategoryBuilder;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.AccessPathBuilder;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.inference.edit.InferenceEditAlgorithm;
import cz.matfyz.inference.edit.algorithms.PrimaryKeyMerge;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingsPair;
import cz.matfyz.inference.schemaconversion.utils.InferenceResult;
import cz.matfyz.tests.example.common.SparkProvider;
import cz.matfyz.transformations.processes.DatabaseToInstance;
import cz.matfyz.wrapperjson.JsonControlWrapper;
import cz.matfyz.wrapperjson.JsonProvider;
import cz.matfyz.wrapperjson.JsonProvider.JsonSettings;

import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;


class DataGenerationTests {

    private final SparkProvider sparkProvider = new SparkProvider();

    @Test
    void testArray() throws Exception {
        final var url = ClassLoader.getSystemResource("inferenceSampleYelpSimpleArray.json");
        final var settings = new JsonSettings(url.toURI().toString(), false, false);
        final var jsonProvider = new JsonProvider(settings);
        final var control = new JsonControlWrapper(jsonProvider).enableSpark(sparkProvider.getSettings());

        final InferenceResult inferenceResult = new MMInferOneInAll()
            .input(control.createProvider())
            .run();

        final List<CategoryMappingsPair> categoryMappingPairs = inferenceResult.pairs();

        final var pair = CategoryMappingsPair.merge(categoryMappingPairs);
        final SchemaCategory schema = pair.schema();
        final List<Mapping> mappings = pair.mappings();

        System.out.println("Mapping:" + mappings.get(0).accessPath());

        final AbstractPullWrapper pullWrapper = control.getPullWrapper();
        final InstanceCategory emptyInstance = new InstanceCategoryBuilder().setSchemaCategory(schema).build();
        final InstanceCategory instance = new DatabaseToInstance().input(mappings.getFirst(), emptyInstance, pullWrapper).run();

        System.out.println("Instance: " + instance);
    }

    @Test
    void testMergeKinds() throws Exception {
        final Path relativePath = Paths.get("src/test/resources/yelpTwoKinds");
        final var url = relativePath.toUri().toURL();
        final var settings = new JsonSettings(url.toURI().toString(), false, false);
        final var jsonProvider = new JsonProvider(settings);
        final var control = new JsonControlWrapper(jsonProvider).enableSpark(sparkProvider.getSettings());

        final var provider = control.createProvider();
        final InferenceResult inferenceResult = new MMInferOneInAll()
            .input(provider)
            .run();

        final List<CategoryMappingsPair> categoryMappingPairs = inferenceResult.pairs();

        final var pair = CategoryMappingsPair.merge(categoryMappingPairs);
        final SchemaCategory schema = pair.schema();
        final MetadataCategory metadata = pair.metadata();
        final List<Mapping> mappings = pair.mappings();

        System.out.println("Mapping A:\n" + mappings.get(0).accessPath());
        System.out.println("Mapping B:\n" + mappings.get(1).accessPath());

        final var pkKey = getKeyFromNames(schema, metadata, "business_id", "business");
        final var pkIdentifiedKey = getKeyFromNames(schema, metadata, "checkin", null);

        final PrimaryKeyMerge edit = (new PrimaryKeyMerge.Data(0, true, pkKey, pkIdentifiedKey, null)).createAlgorithm();
        testAlgorithm(schema, metadata, edit);

        final List<Mapping> editedMappings = edit.applyMappingEdit(mappings);
        Mapping mappingBusiness = null;
        Mapping mappingCheckin = null;

        for (Mapping mapping : editedMappings) {
            if (mapping.kindName().equals("business"))
                mappingBusiness = mapping;
            else
                mappingCheckin = mapping;
        }

        final AccessPathBuilder builder = new AccessPathBuilder();

        final ComplexProperty newComplexProperty = builder.complex(mappingBusiness.kindName(), mappingBusiness.accessPath().signature(), mappingBusiness.accessPath(), mappingCheckin.accessPath());

        final Mapping finalMapping = Mapping.create(provider.getDatasources().stream().findFirst().get(), mappingBusiness.kindName(), schema, mappingBusiness.rootObject().key(), newComplexProperty);
        System.out.println("Mapping C:\n" + finalMapping.accessPath());

        final AbstractPullWrapper pullWrapper = control.getPullWrapper();
        final InstanceCategory emptyInstance = new InstanceCategoryBuilder().setSchemaCategory(schema).build();
        //final InstanceCategory instance = new DatabaseToInstance().input(finalMapping, emptyInstance, pullWrapper).run();

        //System.out.println("Instance: " + instance);
    }

    private Key getKeyFromNames(SchemaCategory schema, MetadataCategory metadata, String name, String domainNameToFind) throws Exception {
        for (final SchemaMorphism morphism : schema.allMorphisms()) {
            final String domainName = metadata.getObject(morphism.dom().key()).label;
            final String codomainName = metadata.getObject(morphism.cod().key()).label;

            if (domainNameToFind != null) {
                if (domainName.equals(domainNameToFind) && codomainName.equals(name))
                    return morphism.cod().key();
            }
            else {
                if (domainName.equals(name))
                    return morphism.dom().key();

                if (codomainName.equals(name))
                    return morphism.cod().key();
            }
        }
        throw new Exception("Key for name " + name + " could not be found");
    }

    private void testAlgorithm(SchemaCategory schema, MetadataCategory metadata, InferenceEditAlgorithm edit) {
        System.out.println("Schema Category before edit:");
        printCategory(schema, metadata);
        System.out.println();

        final var result = edit.applyCategoryEdit(schema, metadata);

        System.out.println("Schema category after edit:");
        printCategory(result.schema(), result.metadata());
    }

    private void printCategory(SchemaCategory schema, MetadataCategory metadata) {
        System.out.println("Objects: ");
        for (SchemaObject o : schema.allObjects()) {
            final var mo = metadata.getObject(o);
            System.out.println("    " + o + " (" + mo.label + ")");
        }

        System.out.println("Morphisms: ");
        for (SchemaMorphism m : schema.allMorphisms()) {
            final var mm = metadata.getMorphism(m);
            System.out.println("    " + m + " (" + mm.label + ")");
        }
    }
}
