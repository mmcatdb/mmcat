package cz.matfyz.tests.inference;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceCategoryBuilder;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.inference.edit.InferenceEditAlgorithm;
import cz.matfyz.inference.edit.algorithms.PrimaryKeyMerge;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
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


public class DataGenerationTests {

    private final SparkProvider sparkProvider = new SparkProvider();

    @Test
    void testArray() throws Exception {
        final var url = ClassLoader.getSystemResource("inferenceSampleYelpSimpleArray.json");
        final var settings = new JsonSettings(url.toURI().toString(), false, false);
        final var jsonProvider = new JsonProvider(settings);
        final var jsonControlWrapper = new JsonControlWrapper(jsonProvider);

        final AbstractInferenceWrapper inferenceWrapper = jsonControlWrapper.getInferenceWrapper(sparkProvider.getSettings());

        final InferenceResult inferenceResult = new MMInferOneInAll()
            .input(inferenceWrapper)
            .run();

        final List<CategoryMappingPair> categoryMappingPairs = inferenceResult.pairs();

        final var pair = CategoryMappingPair.merge(categoryMappingPairs);
        final SchemaCategory schema = pair.schema();
        final List<Mapping> mappings = pair.mappings();

        System.out.println("Mapping:" + mappings.get(0).accessPath());

        final AbstractPullWrapper pullWrapper = jsonControlWrapper.getPullWrapper();
        InstanceCategory instance = new InstanceCategoryBuilder().setSchemaCategory(schema).build();
        instance = new DatabaseToInstance().input(mappings.getFirst(), instance, pullWrapper).run();

        System.out.println("Instance: " + instance);
    }

    @Test
    void testMergeKinds() throws Exception {
        Path relativePath = Paths.get("src/test/resources/yelpTwoKinds");
        final var url = relativePath.toUri().toURL();
        final var settings = new JsonSettings(url.toURI().toString(), false, false);
        final var jsonProvider = new JsonProvider(settings);
        final var jsonControlWrapper = new JsonControlWrapper(jsonProvider);

        final AbstractInferenceWrapper inferenceWrapper = jsonControlWrapper.getInferenceWrapper(sparkProvider.getSettings());

        final InferenceResult inferenceResult = new MMInferOneInAll()
            .input(inferenceWrapper)
            .run();

        final List<CategoryMappingPair> categoryMappingPairs = inferenceResult.pairs();

        final var pair = CategoryMappingPair.merge(categoryMappingPairs);
        final SchemaCategory schema = pair.schema();
        final MetadataCategory metadata = pair.metadata();
        final List<Mapping> mappings = pair.mappings();

        System.out.println("Mapping A : " + mappings.get(0).accessPath());
        System.out.println("Mapping B : " + mappings.get(1).accessPath());

        final var pkKey = getKeyFromNames(schema, metadata, "business_id", "business");
        final var pkIdentifiedKey = getKeyFromNames(schema, metadata, "checkin", null);

        final PrimaryKeyMerge edit = (new PrimaryKeyMerge.Data(0, true, pkKey, pkIdentifiedKey, null)).createAlgorithm();
        testAlgorithm(schema, metadata, edit);
        final List<Mapping> editMappings = edit.applyMappingEdit(mappings);

        final AbstractPullWrapper pullWrapper = jsonControlWrapper.getPullWrapper();
        InstanceCategory instance = new InstanceCategoryBuilder().setSchemaCategory(schema).build();
        instance = new DatabaseToInstance().input(editMappings.getFirst(), instance, pullWrapper).run();

        System.out.println("Instance: " + instance);
    }

    private Key getKeyFromNames(SchemaCategory schema, MetadataCategory metadata, String name, String domainNameToFind) throws Exception {
        for (SchemaMorphism morphism : schema.allMorphisms()) {
            String domainName = metadata.getObject(morphism.dom().key()).label;
            String codomainName = metadata.getObject(morphism.cod().key()).label;

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
            System.out.println("Object: " + o + " label: " + mo.label);
        }

        for (SchemaMorphism m : schema.allMorphisms()) {
            final var mm = metadata.getMorphism(m);
            System.out.println("Dom: " + m.dom() + " cod: " + m.cod() + " sig: " + m.signature() + " label: " + mm.label);
        }
    }
}
