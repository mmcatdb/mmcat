package cz.matfyz.tests.inference;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
//import cz.matfyz.inference.MMInferOneInAll;
//import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.wrapperjson.JsonControlWrapper;
import cz.matfyz.wrapperjson.JsonProvider;
import cz.matfyz.wrapperjson.JsonProvider.JsonSettings;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InferenceTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(InferenceTests.class);


    @Test
    public void testThatFails() {
        // This assertion is guaranteed to fail
        assertTrue(false, "This test is designed to fail");
    }



    @Test
    void basicJsonTest() throws Exception {
        //Integer a = "ahoj";
        /*
        final var url = ClassLoader.getSystemResource("inferenceSample.json");
        // final var url = ClassLoader.getSystemResource("inferenceSampleYelp.json");
        final var settings = new JsonSettings(url.toURI().toString(), false, false);
        final var jsonProvider = new JsonProvider(settings);
        final var sparkSettings = new SparkSettings("local[*]", "./spark");


        final AbstractInferenceWrapper inferenceWrapper = new JsonControlWrapper(jsonProvider).getInferenceWrapper(sparkSettings);

        final CategoryMappingPair categoryMappingPair = new MMInferOneInAll()
            .input(inferenceWrapper, "graf", "new category")
            .run();
        */
        //LOGGER.info("Category mapping pair: {}", categoryMappingPair);
        LOGGER.info("In my inference test");
        System.out.println("In my inference test via print");
    }

}
