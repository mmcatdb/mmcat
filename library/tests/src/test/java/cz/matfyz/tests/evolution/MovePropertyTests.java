package cz.matfyz.tests.evolution;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cz.matfyz.core.exception.MorphismNotFoundException;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataMorphism;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;
import cz.matfyz.core.utils.UniqueIdGenerator;
import cz.matfyz.evolution.category.MoveProperty;
import cz.matfyz.evolution.instance.InstanceEvolver;
import cz.matfyz.tests.example.basic.Schema;

/**
 * Tests for the MOVE operation.
 * MOVE = Copy property to new location + Delete original.
 */
class MovePropertyTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    /**
     * Helper to count rows in an objex.
     */
    private int countRows(cz.matfyz.core.instance.InstanceObjex objex) {
        int count = 0;
        for (@SuppressWarnings("unused") var ignored : objex.allRows()) {
            count++;
        }
        return count;
    }

    record MovedMorphismSpec(int sourceSig, int targetSig, int propertyKey, String label) {}

    /**
     * Helper to create a MoveProperty operation.
     */
    private MoveProperty createMoveOperation(
            Key sourceKey,
            Key targetKey,
            List<MovedMorphismSpec> movedMorphismSpecs
    ) {
        final var movedMorphisms = movedMorphismSpecs.stream().map(spec -> {
            final var sourceSig = new BaseSignature(spec.sourceSig);
            final var targetSig = new BaseSignature(spec.targetSig);
            final var propertyKey = new Key(spec.propertyKey);

            final var sourceMorphism = new SerializedMorphism(sourceSig, sourceKey, propertyKey, Min.ZERO, Set.of());
            final var sourceMetadata = new SerializedMetadataMorphism(sourceSig, spec.label);

            final var targetMorphism = new SerializedMorphism(targetSig, targetKey, propertyKey, Min.ZERO, Set.of());
            final var targetMetadata = new SerializedMetadataMorphism(targetSig, spec.label);

            return new MoveProperty.MovedMorphism(sourceMorphism, sourceMetadata, targetMorphism, targetMetadata);
        }).toList();

        return new MoveProperty(
            sourceKey,
            targetKey,
            movedMorphisms
        );
    }

    /**
     * Test MOVE schema changes - creates target morphism, deletes source morphism.
     */
    @Test
    void movePropertySchemaChanges() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Verify initial state - Order.number exists (signature 1, Order key=1, number key=2)
        assertNotNull(schema.getMorphism(new BaseSignature(1)), "Order -> number morphism should exist initially");

        // Move order.number to a new signature (simulating move within same object or different object)
        // For simplicity, we'll move it to a new signature on the same object
        final var moveOperation = createMoveOperation(
            Schema.order.key(),       // source: Order
            Schema.order.key(),       // target: Order (same object, different signature)
            List.of(new MovedMorphismSpec(1, 3001, 2, "number"))  // old sig=1, new sig=3001
        );

        moveOperation.up(schema, metadata);

        // Verify: old morphism deleted, new morphism created
        assertThrows(MorphismNotFoundException.class, () -> schema.getMorphism(new BaseSignature(1)),
            "Original morphism should be deleted");
        assertNotNull(schema.getMorphism(new BaseSignature(3001)),
            "New morphism should be created");
    }

    /**
     * Test MOVE down() reverts the operation.
     */
    @Test
    void movePropertyDownReverts() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        final var moveOperation = createMoveOperation(
            Schema.order.key(),
            Schema.order.key(),
            List.of(new MovedMorphismSpec(1, 3001, 2, "number"))
        );

        // Apply move
        moveOperation.up(schema, metadata);
        assertThrows(MorphismNotFoundException.class, () -> schema.getMorphism(new BaseSignature(1)));
        assertNotNull(schema.getMorphism(new BaseSignature(3001)));

        // Revert move
        moveOperation.down(schema, metadata);
        assertNotNull(schema.getMorphism(new BaseSignature(1)), "Original morphism should be restored");
        assertThrows(MorphismNotFoundException.class, () -> schema.getMorphism(new BaseSignature(3001)),
            "Target morphism should be deleted");
    }

    /**
     * Test MOVE instance data migration - values are moved to new morphism.
     */
    @Test
    void movePropertyInstanceMigratesData() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Build initial instance with orders
        final var builder = new InstanceBuilder(schema);
        builder.value(Schema.order_number, "o_100").objex(Schema.order);
        builder.value(Schema.order_number, "o_200").objex(Schema.order);
        final InstanceCategory instance = builder.build();

        // Verify initial state
        final var orderObjex = instance.getObjex(Schema.order.key());
        assertEquals(2, countRows(orderObjex));

        // Move order.number to new signature
        final var moveOperation = createMoveOperation(
            Schema.order.key(),
            Schema.order.key(),
            List.of(new MovedMorphismSpec(1, 3001, 2, "number"))
        );

        // Apply schema change first
        moveOperation.up(schema, metadata);

        // Apply instance change
        final var evolver = new InstanceEvolver(instance);
        moveOperation.accept(evolver);

        // Verify: property values are now accessible via new signature
        final var newSig = new BaseSignature(3001);
        final var values = new java.util.ArrayList<String>();
        for (final var row : orderObjex.allRows()) {
            final var value = row.tryGetScalarValue(newSig);
            if (value != null) {
                values.add(value);
            }
        }
        assertEquals(2, values.size(), "Should have 2 property values via new signature");
        assertTrue(values.contains("o_100"), "Should contain value o_100");
        assertTrue(values.contains("o_200"), "Should contain value o_200");
    }

    /**
     * Test MOVE with multiple properties.
     */
    @Test
    void moveMultipleProperties() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Build instance with product data
        final var builder = new InstanceBuilder(schema);
        builder
            .value(Schema.product_id, "p_1")
            .value(Schema.product_label, "Laptop")
            .value(Schema.product_price, "999.99")
            .objex(Schema.product);
        builder
            .value(Schema.product_id, "p_2")
            .value(Schema.product_label, "Mouse")
            .value(Schema.product_price, "29.99")
            .objex(Schema.product);
        final InstanceCategory instance = builder.build();

        // Move label and price to new signatures
        // product_label: sig 55, product -> label (key 55)
        // product_price: sig 56, product -> price (key 56)
        final var moveOperation = createMoveOperation(
            Schema.product.key(),
            Schema.product.key(),
            List.of(
                new MovedMorphismSpec(55, 4001, 55, "label"),
                new MovedMorphismSpec(56, 4002, 56, "price")
            )
        );

        // Apply schema and instance changes
        moveOperation.up(schema, metadata);
        final var evolver = new InstanceEvolver(instance);
        moveOperation.accept(evolver);

        // Verify: old morphisms deleted
        assertThrows(MorphismNotFoundException.class, () -> schema.getMorphism(new BaseSignature(55)));
        assertThrows(MorphismNotFoundException.class, () -> schema.getMorphism(new BaseSignature(56)));

        // Verify: new morphisms created
        assertNotNull(schema.getMorphism(new BaseSignature(4001)));
        assertNotNull(schema.getMorphism(new BaseSignature(4002)));

        // Verify: property values migrated to new signatures
        final var productObjex = instance.getObjex(Schema.product.key());
        final var newLabelSig = new BaseSignature(4001);
        final var newPriceSig = new BaseSignature(4002);
        final var labelValues = new java.util.ArrayList<String>();
        final var priceValues = new java.util.ArrayList<String>();

        for (final var row : productObjex.allRows()) {
            final var labelValue = row.tryGetScalarValue(newLabelSig);
            final var priceValue = row.tryGetScalarValue(newPriceSig);
            if (labelValue != null) labelValues.add(labelValue);
            if (priceValue != null) priceValues.add(priceValue);
        }

        assertEquals(2, labelValues.size(), "Should have 2 label values");
        assertEquals(2, priceValues.size(), "Should have 2 price values");
        assertTrue(labelValues.contains("Laptop"), "Should contain label Laptop");
        assertTrue(labelValues.contains("Mouse"), "Should contain label Mouse");
        assertTrue(priceValues.contains("999.99"), "Should contain price 999.99");
        assertTrue(priceValues.contains("29.99"), "Should contain price 29.99");
    }

}
