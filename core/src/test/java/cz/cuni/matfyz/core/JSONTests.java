package cz.cuni.matfyz.core;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.DynamicName;
import cz.cuni.matfyz.core.mapping.Name;
import cz.cuni.matfyz.core.mapping.StaticName;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.utils.UniqueIdProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
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

    private TestData data;


    @BeforeEach
    public void setUp() {
        UniqueIdProvider.reset();
        data = new TestData();
    }

    @Test
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
    public void name() throws Exception {
        final var staticName = new StaticName("Static Name");
        final var anonymousName = StaticName.createAnonymous();
        final var dynamicName = new DynamicName(Signature.createBase(69));
        
        LOGGER.info(mapper.writeValueAsString(staticName));
        LOGGER.info(mapper.writeValueAsString(mapper.readValue(mapper.writeValueAsString(staticName), Name.class)));
        LOGGER.info(mapper.writeValueAsString(anonymousName));
        LOGGER.info(mapper.writeValueAsString(mapper.readValue(mapper.writeValueAsString(anonymousName), Name.class)));
        LOGGER.info(mapper.writeValueAsString(dynamicName));
        LOGGER.info(mapper.writeValueAsString(mapper.readValue(mapper.writeValueAsString(dynamicName), Name.class)));
    }

}
