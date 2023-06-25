package cz.cuni.matfyz.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.DynamicName;
import cz.cuni.matfyz.core.mapping.SimpleProperty;
import cz.cuni.matfyz.core.mapping.StaticName;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.tests.TestData;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class JsonTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonTests.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final TestData data = new TestData();

    @Test
    public void signature() {
        final var empty = Signature.createEmpty();
        fullTest(empty);

        final var base = Signature.createBase(69);
        fullTest(base);

        final var composite = Signature.createBase(42).concatenate(Signature.createBase(69));
        fullTest(composite);
    }

    @Test
    public void name() {
        final var anonymous = StaticName.createAnonymous();
        fullTest(anonymous);

        final var _static = new StaticName("Static name");
        fullTest(_static);

        final var dynamic = new DynamicName(Signature.createBase(69));
        fullTest(dynamic);
    }

    // TODO reorganize and rename later
    public void toJsonConversion() throws Exception {
        final var category = data.createDefaultSchemaCategory();

        final var order = category.getObject(data.orderKey);
        
        LOGGER.info(mapper.writeValueAsString(order.key()));
        LOGGER.info(mapper.writeValueAsString(mapper.readValue(mapper.writeValueAsString(order.key()), Key.class)));
        LOGGER.info(mapper.writeValueAsString(order));
        LOGGER.info(mapper.writeValueAsString(mapper.readValue(mapper.writeValueAsString(order), SchemaObject.class)));


        final var json = """
            {"ids": {"type": "Signatures", "signatureIds": [["1"]]}, "key": {"value": 1}, "label": "Customer", "superId": ["1"]}
            """;

        LOGGER.info(mapper.writeValueAsString(mapper.readValue(json, SchemaObject.class)));
    }

    @Test
    public void accessPath() {
        final var simple = new SimpleProperty("number", data.orderToNumber);
        fullTest(simple);

        final var complex = data.path_orderRoot();
        fullTest(complex);

        final var nested = data.path_nestedDoc();
        fullTest(nested);
    }

    private record ExceptionData(
        String a,
        int b
    ) implements Serializable {}

    @Test
    public void namedException() {
        final var simple = new TestException("simple", null, null);
        LOGGER.info(simple.toString());
        serializationTest(simple);

        final var stringData = new TestException("stringData", "some string", null);
        LOGGER.info(stringData.toString());
        serializationTest(stringData);

        final var classData = new TestException("classData", new ExceptionData("other string", 69), null);
        LOGGER.info(classData.toString());
        serializationTest(classData);

        final var cause = new TestException("classData", new ExceptionData("other string", 69), new UnsupportedOperationException("Something is unsupported"));
        LOGGER.info(cause.toString());
        serializationTest(cause);
    }

    private String serialize(Object object) throws IOException {
        return mapper.writeValueAsString(object);
    }

    private Object deserialize(String json) throws IOException {
        return mapper.readValue(json, Object.class);
    }

    private class Output { String value = ""; }

    private static String WHITE_COLOR_CODE = "\u001b[1;37m";

    private void fullTest(Object object) {
        final Output json = serializationTest(object);

        final Output secondJson = new Output();
        assertDoesNotThrow(() -> {
            secondJson.value = serialize(deserialize(json.value));
        });
        assertEquals(json.value, secondJson.value);
    }

    private Output serializationTest(Object object) {
        final Output json = new Output();
        assertDoesNotThrow(() -> {
            json.value = serialize(object);
            LOGGER.info("\n{}Original:\n{}\n{}Serialized:\n{}", WHITE_COLOR_CODE, object, WHITE_COLOR_CODE, json.value);
        });

        return json;
    }

}
