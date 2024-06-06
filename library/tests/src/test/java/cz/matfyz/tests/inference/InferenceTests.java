package cz.matfyz.tests.inference;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.wrapperjson.JsonControlWrapper;
import cz.matfyz.wrapperjson.JsonProvider;
import cz.matfyz.wrapperjson.JsonProvider.JsonSettings;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InferenceTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(InferenceTests.class);

    @Test
    void basicJsonTest() throws Exception {
        final var url = ClassLoader.getSystemResource("inferenceSample.json");
        // final var url = ClassLoader.getSystemResource("inferenceSampleYelp.json");
        final var settings = new JsonSettings(url.toURI().toString(), false, false);
        final var jsonProvider = new JsonProvider(settings);
        final var sparkSettings = new SparkSettings("local[*]", "./spark");


        final AbstractInferenceWrapper inferenceWrapper = new JsonControlWrapper(jsonProvider).getInferenceWrapper(sparkSettings);

        final CategoryMappingPair categoryMappingPair = new MMInferOneInAll()
            .input(inferenceWrapper, "graf", "new category")
            .run();

        LOGGER.info("Category mapping pair: {}", categoryMappingPair);
    }

}
