package cz.matfyz.tests.utils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonTestBase.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final boolean isVerbose;

    public JsonTestBase(boolean isVerbose) {
        this.isVerbose = isVerbose;
    }
    
    private static String serialize(Object object) throws IOException {
        return mapper.writeValueAsString(object);
    }

    private static Object deserialize(String json) throws IOException {
        return mapper.readValue(json, Object.class);
    }

    private static class Output { String value = ""; }

    private static final String WHITE_COLOR_CODE = "\u001b[1;37m";

    public void fullTest(Object object) {
        final Output json = serializationTest(object);

        final Output secondJson = new Output();
        assertDoesNotThrow(() -> {
            secondJson.value = serialize(deserialize(json.value));
        });
        assertEquals(json.value, secondJson.value);
    }

    public Output serializationTest(Object object) {
        final Output json = new Output();
        assertDoesNotThrow(() -> {
            json.value = serialize(object);
            if (isVerbose)
                LOGGER.info("\n{}Original:\n{}\n{}Serialized:\n{}", WHITE_COLOR_CODE, object, WHITE_COLOR_CODE, json.value);
        });

        return json;
    }

}
