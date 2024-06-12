package cz.matfyz.tests.inference;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.inference.schemaconversion.utils.CategoryMappingPair;
import cz.matfyz.wrapperjson.JsonControlWrapper;
import cz.matfyz.wrapperjson.JsonProvider;
import cz.matfyz.wrapperjson.JsonProvider.JsonSettings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InferenceTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(InferenceTests.class);

    @Test
    public void testThatFails() {
        assertTrue(false, "This test is designed to fail");
    }

    @Test
    void basicJsonTest() throws Exception {

        LOGGER.info("In my inference test");
        
        final var url = ClassLoader.getSystemResource("inferenceSampleUnesco.json");
        // final var url = ClassLoader.getSystemResource("inferenceSampleYelp.json");
        final var settings = new JsonSettings(url.toURI().toString(), false, false);
        final var jsonProvider = new JsonProvider(settings);
        final var sparkSettings = new SparkSettings("local[*]", "./spark");


        final AbstractInferenceWrapper inferenceWrapper = new JsonControlWrapper(jsonProvider).getInferenceWrapper(sparkSettings);

        final CategoryMappingPair categoryMappingPair = new MMInferOneInAll()
            .input(inferenceWrapper, "graph", "new category")
            .run();        
    }

    
    @Test
    void keysTest() throws Exception {
       
        final var url = ClassLoader.getSystemResource("inferenceSampleUnesco.json");
        // final var url = ClassLoader.getSystemResource("inferenceSampleYelp.json");
        final var settings = new JsonSettings(url.toURI().toString(), false, false);
        final var jsonProvider = new JsonProvider(settings);
        final var sparkSettings = new SparkSettings("local[*]", "./spark");

        final AbstractInferenceWrapper inferenceWrapper = new JsonControlWrapper(jsonProvider).getInferenceWrapper(sparkSettings);

        final CategoryMappingPair categoryMappingPair = new MMInferOneInAll()
            .input(inferenceWrapper, "graph", "New Category")
            .run();
        
        LOGGER.info("Schema Category Keys: {}", categoryMappingPair.schemaCategory().allObjects());

        int expectedNumOfKeys = 26; 
        assertEquals(expectedNumOfKeys, categoryMappingPair.schemaCategory().allObjects().size(), "The number of objects is not as expected.");   
        
    }

}
