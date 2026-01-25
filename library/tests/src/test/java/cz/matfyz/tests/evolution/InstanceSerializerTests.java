package cz.matfyz.tests.evolution;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceSerializer;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.UniqueIdGenerator;
import cz.matfyz.tests.example.basic.Schema;

/**
 * Tests for InstanceSerializer - verifies that property values are properly
 * serialized and deserialized, which is critical for data propagation.
 */
class InstanceSerializerTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    /**
     * Test that property values added via addPropertyValue() survive serialization/deserialization.
     * This is critical for GROUP, JOIN, SPLIT operations which use propertyValues.
     */
    @Test
    void propertyValuesAreSerialized() {
        final SchemaCategory schema = Schema.newSchema();

        // Build initial instance
        final var builder = new InstanceBuilder(schema);
        builder.value(Schema.order_number, "o_100").objex(Schema.order);
        builder.value(Schema.order_number, "o_200").objex(Schema.order);
        final InstanceCategory instance = builder.build();

        // Add property values (simulating what GROUP/JOIN/SPLIT do)
        final var customSig = new BaseSignature(9999);
        final var orderObjex = instance.getObjex(Schema.order.key());
        for (final var row : orderObjex.allRows()) {
            row.addPropertyValue(customSig, "custom_value_" + row.superId.getValue(new BaseSignature(1)));
        }

        // Verify values exist before serialization
        for (final var row : orderObjex.allRows()) {
            assertNotNull(row.tryGetScalarValue(customSig), "Property value should exist before serialization");
        }

        // Serialize
        final var serialized = InstanceSerializer.serialize(instance);

        // Deserialize into a new instance
        final InstanceCategory deserialized = InstanceSerializer.deserialize(serialized, schema);

        // Verify property values are preserved after deserialization
        final var deserializedObjex = deserialized.getObjex(Schema.order.key());
        int count = 0;
        for (final var row : deserializedObjex.allRows()) {
            final var value = row.tryGetScalarValue(customSig);
            assertNotNull(value, "Property value should exist after deserialization");
            assertTrue(value.startsWith("custom_value_"), "Property value should be preserved: " + value);
            count++;
        }
        assertEquals(2, count, "Should have 2 rows with property values");
    }

    /**
     * Test that multiple property values per row are preserved.
     */
    @Test
    void multiplePropertyValuesAreSerialized() {
        final SchemaCategory schema = Schema.newSchema();

        final var builder = new InstanceBuilder(schema);
        builder
            .value(Schema.product_id, "p_1")
            .value(Schema.product_label, "Laptop")
            .objex(Schema.product);
        final InstanceCategory instance = builder.build();

        // Add multiple property values
        final var sig1 = new BaseSignature(8001);
        final var sig2 = new BaseSignature(8002);
        final var sig3 = new BaseSignature(8003);

        final var productObjex = instance.getObjex(Schema.product.key());
        for (final var row : productObjex.allRows()) {
            row.addPropertyValue(sig1, "value1");
            row.addPropertyValue(sig2, "value2");
            row.addPropertyValue(sig3, "value3");
        }

        // Serialize and deserialize
        final var serialized = InstanceSerializer.serialize(instance);
        final InstanceCategory deserialized = InstanceSerializer.deserialize(serialized, schema);

        // Verify all values preserved
        final var deserializedObjex = deserialized.getObjex(Schema.product.key());
        for (final var row : deserializedObjex.allRows()) {
            assertEquals("value1", row.tryGetScalarValue(sig1));
            assertEquals("value2", row.tryGetScalarValue(sig2));
            assertEquals("value3", row.tryGetScalarValue(sig3));
        }
    }

    /**
     * Test that empty property values map doesn't cause issues.
     */
    @Test
    void emptyPropertyValuesHandledCorrectly() {
        final SchemaCategory schema = Schema.newSchema();

        final var builder = new InstanceBuilder(schema);
        builder.value(Schema.order_number, "o_100").objex(Schema.order);
        final InstanceCategory instance = builder.build();

        // Don't add any property values - verify serialization still works
        final var serialized = InstanceSerializer.serialize(instance);
        final InstanceCategory deserialized = InstanceSerializer.deserialize(serialized, schema);

        // Verify instance is valid
        final var deserializedObjex = deserialized.getObjex(Schema.order.key());
        int count = 0;
        for (final var row : deserializedObjex.allRows()) {
            assertEquals("o_100", row.superId.getValue(new BaseSignature(1)));
            count++;
        }
        assertEquals(1, count);
    }

}
