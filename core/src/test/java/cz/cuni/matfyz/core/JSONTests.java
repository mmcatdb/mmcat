package cz.cuni.matfyz.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.DynamicName;
import cz.cuni.matfyz.core.mapping.SimpleProperty;
import cz.cuni.matfyz.core.mapping.StaticName;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.utils.UniqueIdProvider;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class JSONTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSONTests.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final TestData data = new TestData();

    @BeforeEach
    public void setUp() {
        UniqueIdProvider.reset();
    }

    @Test
    public void signature() {
        final var empty = Signature.createEmpty();
        testAlgorithm(empty);

        final var base = Signature.createBase(69);
        testAlgorithm(base);

        final var composite = Signature.createBase(42).concatenate(Signature.createBase(69));
        testAlgorithm(composite);
    }

    @Test
    public void name() {
        final var anonymous = StaticName.createAnonymous();
        testAlgorithm(anonymous);

        final var _static = new StaticName("Static name");
        testAlgorithm(_static);

        final var dynamic = new DynamicName(Signature.createBase(69));
        testAlgorithm(dynamic);
    }

    @Test
    // TODO reorganize and rename later
    public void toJSONConversion() throws Exception {
        final var category = data.createDefaultSchemaCategory();

        final var order = category.getObject(data.orderKey);
        
        LOGGER.info(mapper.writeValueAsString(order.key()));
        LOGGER.info(mapper.writeValueAsString(mapper.readValue(mapper.writeValueAsString(order.key()), Key.class)));
        LOGGER.info(mapper.writeValueAsString(order));
        LOGGER.info(mapper.writeValueAsString(mapper.readValue(mapper.writeValueAsString(order), SchemaObject.class)));


        final var json = """
            {"ids": {"type": "Signatures", "signatureIds": [[[1]]]}, "key": {"value": 1}, "label": "Customer", "superId": [[1]]}
            """;

        LOGGER.info(mapper.writeValueAsString(mapper.readValue(json, SchemaObject.class)));
    }

    @Test
    public void accessPath() {
        final var simple = new SimpleProperty("number", data.orderToNumber);
        testAlgorithm(simple);

        final var complex = data.path_orderRoot();
        testAlgorithm(complex);

        final var nested = data.path_nestedDoc();
        testAlgorithm(nested);
    }

    private String serialize(Object object) throws IOException {
        return mapper.writeValueAsString(object);
    }

    private Object deserialize(String json) throws IOException {
        return mapper.readValue(json, Object.class);
    }

    private class Output { String value = ""; }

    private static String WHITE_COLOR_CODE = "\u001b[1;37m";

    private void testAlgorithm(Object object) {
        final Output json = new Output();

        assertDoesNotThrow(() -> {
            json.value = serialize(object);
            LOGGER.info("\n{}Original:\n{}\n{}Serialized:\n{}", WHITE_COLOR_CODE, object, WHITE_COLOR_CODE, json.value);
        });

        final Output secondJson = new Output();

        assertDoesNotThrow(() -> {
            secondJson.value = serialize(deserialize(json.value));
        });

        assertEquals(json.value, secondJson.value);
    }

}
