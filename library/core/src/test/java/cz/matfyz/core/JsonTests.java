package cz.matfyz.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    @Test
    public void signature() throws JsonProcessingException, IOException {
        final var empty = Signature.createEmpty();
        fullTest(empty);

        final var base = Signature.createBase(69);
        fullTest(base);

        final var composite = Signature.createBase(42).concatenate(Signature.createBase(69));
        fullTest(composite);

        final Signature emptyParsed = mapper.readValue(serialize(empty), Signature.class);
        assertInstanceOf(Signature.class, emptyParsed);
        final Signature baseParsed = mapper.readValue(serialize(base), Signature.class);
        assertInstanceOf(BaseSignature.class, baseParsed);
        final Signature compositeParsed = mapper.readValue(serialize(composite), Signature.class);
        assertInstanceOf(Signature.class, compositeParsed);
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

    @Test
    public void accessPath() {
        final var simple = new SimpleProperty("simple", Signature.createBase(1));
        fullTest(simple);

        final var complex = ComplexProperty.create("complex", Signature.createBase(2),
            simple,
            new SimpleProperty("simple2", Signature.createBase(3))
        );
        fullTest(complex);

        final var path = ComplexProperty.createRoot(
            complex,
            ComplexProperty.createAuxiliary(new StaticName("auxiliary")),
            ComplexProperty.create("dynamic", Signature.createBase(4).concatenate(Signature.createBase(5)),
                new SimpleProperty(Signature.createBase(6), Signature.createBase(7))
            )
        );
        fullTest(path);
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
