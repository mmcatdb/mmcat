package cz.matfyz.tests.evolution;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cz.matfyz.core.exception.MorphismNotFoundException;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataMorphism;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObjex;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;
import cz.matfyz.core.utils.UniqueIdGenerator;
import cz.matfyz.evolution.category.GroupProperties;
import cz.matfyz.evolution.category.UngroupProperty;
import cz.matfyz.evolution.instance.InstanceEvolver;
import cz.matfyz.tests.example.basic.Schema;

/**
 * Tests for the UNGROUP operation.
 * UNGROUP is the inverse of GROUP - it moves properties from a grouping object back to the parent.
 */
class UngroupPropertyTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    /**
     * Helper to count rows in an objex.
     */
    private int countRows(cz.matfyz.core.instance.InstanceObjex objex) {
        int count = 0;
        for (var row : objex.allRows()) {
            count++;
        }
        return count;
    }

    record MovedMorphismSpec(int oldSig, int newSig, int propertyKey, String label) {}

    /**
     * Helper to create a GroupProperties operation.
     */
    private GroupProperties createGroupOperation(
            Key parentKey,
            int groupingObjexKeyInt,
            String groupingLabel,
            int parentToGroupSigInt,
            List<MovedMorphismSpec> movedMorphismSpecs
    ) {
        final var groupingObjexKey = new Key(groupingObjexKeyInt);
        final var groupingObjex = new SerializedObjex(groupingObjexKey, ObjexIds.fromIds(SignatureId.empty()));
        final var groupingObjexMetadata = new SerializedMetadataObjex(groupingObjexKey, groupingLabel, null);

        final var parentToGroupSig = new BaseSignature(parentToGroupSigInt);
        final var parentToGroupMorphism = new SerializedMorphism(
            parentToGroupSig,
            parentKey,
            groupingObjexKey,
            Min.ONE,
            Set.of()
        );
        final var parentToGroupMorphismMetadata = new SerializedMetadataMorphism(parentToGroupSig, groupingLabel);

        final var movedMorphisms = movedMorphismSpecs.stream().map(spec -> {
            final var oldSig = new BaseSignature(spec.oldSig);
            final var newSig = new BaseSignature(spec.newSig);
            final var propertyKey = new Key(spec.propertyKey);

            final var oldMorphism = new SerializedMorphism(oldSig, parentKey, propertyKey, Min.ZERO, Set.of());
            final var oldMetadata = new SerializedMetadataMorphism(oldSig, spec.label);

            final var newMorphism = new SerializedMorphism(newSig, groupingObjexKey, propertyKey, Min.ZERO, Set.of());
            final var newMetadata = new SerializedMetadataMorphism(newSig, spec.label);

            return new GroupProperties.MovedMorphism(oldMorphism, oldMetadata, newMorphism, newMetadata);
        }).toList();

        return new GroupProperties(
            parentKey,
            groupingObjex,
            groupingObjexMetadata,
            parentToGroupMorphism,
            parentToGroupMorphismMetadata,
            movedMorphisms
        );
    }

    /**
     * Helper to create an UngroupProperty operation (inverse of group).
     */
    private UngroupProperty createUngroupOperation(
            Key parentKey,
            int groupingObjexKeyInt,
            String groupingLabel,
            int parentToGroupSigInt,
            List<MovedMorphismSpec> movedMorphismSpecs
    ) {
        final var groupingObjexKey = new Key(groupingObjexKeyInt);
        final var groupingObjex = new SerializedObjex(groupingObjexKey, ObjexIds.fromIds(SignatureId.empty()));
        final var groupingObjexMetadata = new SerializedMetadataObjex(groupingObjexKey, groupingLabel, null);

        final var parentToGroupSig = new BaseSignature(parentToGroupSigInt);
        final var parentToGroupMorphism = new SerializedMorphism(
            parentToGroupSig,
            parentKey,
            groupingObjexKey,
            Min.ONE,
            Set.of()
        );
        final var parentToGroupMorphismMetadata = new SerializedMetadataMorphism(parentToGroupSig, groupingLabel);

        // For UNGROUP: old = grouping->property, new = parent->property
        final var movedMorphisms = movedMorphismSpecs.stream().map(spec -> {
            final var oldSig = new BaseSignature(spec.oldSig);  // grouping -> property
            final var newSig = new BaseSignature(spec.newSig);  // parent -> property
            final var propertyKey = new Key(spec.propertyKey);

            final var oldMorphism = new SerializedMorphism(oldSig, groupingObjexKey, propertyKey, Min.ZERO, Set.of());
            final var oldMetadata = new SerializedMetadataMorphism(oldSig, spec.label);

            final var newMorphism = new SerializedMorphism(newSig, parentKey, propertyKey, Min.ZERO, Set.of());
            final var newMetadata = new SerializedMetadataMorphism(newSig, spec.label);

            return new UngroupProperty.MovedMorphism(oldMorphism, oldMetadata, newMorphism, newMetadata);
        }).toList();

        return new UngroupProperty(
            parentKey,
            groupingObjex,
            groupingObjexMetadata,
            parentToGroupMorphism,
            parentToGroupMorphismMetadata,
            movedMorphisms
        );
    }

    /**
     * First GROUP some properties, then UNGROUP them to verify the inverse operation.
     */
    @Test
    void ungroupRevertsGroupSchemaChanges() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // First, apply GROUP to set up the "grouped" state
        // Group: Order.number -> Order.details.number
        // Old morphism: Order (key=1) -> number (key=2) via signature 1
        final var groupOperation = createGroupOperation(
            Schema.order.key(),
            1000,
            "details",
            1001,
            List.of(new MovedMorphismSpec(1, 1002, 2, "number"))
        );

        groupOperation.up(schema, metadata);

        // Verify grouped state
        assertNotNull(schema.getObjex(new Key(1000)), "Grouping objex should exist after GROUP");
        assertNotNull(schema.getMorphism(new BaseSignature(1001)), "Parent to group morphism should exist");
        assertNotNull(schema.getMorphism(new BaseSignature(1002)), "Group to property morphism should exist");
        assertThrows(MorphismNotFoundException.class, () -> schema.getMorphism(new BaseSignature(1)), "Old direct morphism should be deleted");

        // Now create and apply UNGROUP
        // UNGROUP moves number from details back to Order
        // old = details->number (sig 1002), new = Order->number (sig 1)
        final var ungroupOperation = createUngroupOperation(
            Schema.order.key(),
            1000,
            "details",
            1001,
            List.of(new MovedMorphismSpec(1002, 1, 2, "number"))
        );

        ungroupOperation.up(schema, metadata);

        // Verify UNGROUP reverted to original state
        // Note: SchemaCategory.getObjex() returns null for missing keys (doesn't throw)
        assertNull(schema.getObjex(new Key(1000)), "Grouping objex should be deleted after UNGROUP");
        assertThrows(MorphismNotFoundException.class, () -> schema.getMorphism(new BaseSignature(1001)), "Parent to group morphism should be deleted");
        assertThrows(MorphismNotFoundException.class, () -> schema.getMorphism(new BaseSignature(1002)), "Group to property morphism should be deleted");
        assertNotNull(schema.getMorphism(new BaseSignature(1)), "Original direct morphism should be restored");
    }

    /**
     * Test that UNGROUP's down() method reverts to the grouped state.
     */
    @Test
    void ungroupDownRevertsToGroupedState() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // First GROUP, then UNGROUP
        final var groupOperation = createGroupOperation(
            Schema.order.key(),
            1000,
            "details",
            1001,
            List.of(new MovedMorphismSpec(1, 1002, 2, "number"))
        );
        groupOperation.up(schema, metadata);

        final var ungroupOperation = createUngroupOperation(
            Schema.order.key(),
            1000,
            "details",
            1001,
            List.of(new MovedMorphismSpec(1002, 1, 2, "number"))
        );
        ungroupOperation.up(schema, metadata);

        // Verify ungrouped state (getObjex returns null for missing keys)
        assertNull(schema.getObjex(new Key(1000)));

        // Apply down() to revert to grouped state
        ungroupOperation.down(schema, metadata);

        // Verify back to grouped state
        assertNotNull(schema.getObjex(new Key(1000)), "Grouping objex should be recreated");
        assertNotNull(schema.getMorphism(new BaseSignature(1001)), "Parent to group morphism should be recreated");
        assertNotNull(schema.getMorphism(new BaseSignature(1002)), "Group to property morphism should be recreated");
    }

    /**
     * Test that UNGROUP migrates instance data correctly.
     */
    @Test
    void ungroupInstanceMigratesData() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Build initial instance
        final var builder = new InstanceBuilder(schema);
        builder.value(Schema.order_number, "o_100").objex(Schema.order);
        builder.value(Schema.order_number, "o_200").objex(Schema.order);
        final InstanceCategory instance = builder.build();

        // Apply GROUP first
        final var groupOperation = createGroupOperation(
            Schema.order.key(),
            1000,
            "details",
            1001,
            List.of(new MovedMorphismSpec(1, 1002, 2, "number"))
        );
        groupOperation.up(schema, metadata);

        final var groupEvolver = new InstanceEvolver(instance);
        groupOperation.accept(groupEvolver);

        // Verify grouped state
        final var groupingObjex = instance.getObjex(new Key(1000));
        assertNotNull(groupingObjex);
        assertEquals(2, countRows(groupingObjex));

        // Now apply UNGROUP
        final var ungroupOperation = createUngroupOperation(
            Schema.order.key(),
            1000,
            "details",
            1001,
            List.of(new MovedMorphismSpec(1002, 1, 2, "number"))
        );
        ungroupOperation.up(schema, metadata);

        final var ungroupEvolver = new InstanceEvolver(instance);
        ungroupOperation.accept(ungroupEvolver);

        // Verify: grouping objex was deleted
        assertNull(instance.getObjex(new Key(1000)), "Grouping objex should be deleted");

        // Verify: parent objex still has rows
        final var orderObjex = instance.getObjex(Schema.order.key());
        assertNotNull(orderObjex);
        assertEquals(2, countRows(orderObjex));

        // Verify: property values were migrated back to parent rows
        final var orderNumberSig = new BaseSignature(1);
        final var values = new java.util.ArrayList<String>();
        for (final var row : orderObjex.allRows()) {
            final var value = row.tryGetScalarValue(orderNumberSig);
            if (value != null) {
                values.add(value);
            }
        }
        assertEquals(2, values.size(), "Should have 2 property values in parent rows");
        assertTrue(values.contains("o_100"), "Should contain value o_100");
        assertTrue(values.contains("o_200"), "Should contain value o_200");
    }

    /**
     * Test UNGROUP with multiple properties.
     */
    @Test
    void ungroupWithMultipleProperties() {
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

        // First GROUP label and price under productDetails
        // product_label: sig 55, product -> label (key 55)
        // product_price: sig 56, product -> price (key 56)
        final var groupOperation = createGroupOperation(
            Schema.product.key(),
            2000,
            "productDetails",
            2001,
            List.of(
                new MovedMorphismSpec(55, 2002, 55, "label"),
                new MovedMorphismSpec(56, 2003, 56, "price")
            )
        );
        groupOperation.up(schema, metadata);
        final var groupEvolver = new InstanceEvolver(instance);
        groupOperation.accept(groupEvolver);

        // Verify grouped state
        var groupingObjex = instance.getObjex(new Key(2000));
        assertNotNull(groupingObjex);
        assertEquals(2, countRows(groupingObjex));

        // Now UNGROUP
        final var ungroupOperation = createUngroupOperation(
            Schema.product.key(),
            2000,
            "productDetails",
            2001,
            List.of(
                new MovedMorphismSpec(2002, 55, 55, "label"),
                new MovedMorphismSpec(2003, 56, 56, "price")
            )
        );
        ungroupOperation.up(schema, metadata);
        final var ungroupEvolver = new InstanceEvolver(instance);
        ungroupOperation.accept(ungroupEvolver);

        // Verify: grouping objex deleted
        assertNull(instance.getObjex(new Key(2000)), "Grouping objex should be deleted");

        // Verify: property values migrated back to product rows
        final var productObjex = instance.getObjex(Schema.product.key());
        final var labelSig = new BaseSignature(55);
        final var priceSig = new BaseSignature(56);
        final var labelValues = new java.util.ArrayList<String>();
        final var priceValues = new java.util.ArrayList<String>();

        for (final var row : productObjex.allRows()) {
            final var labelValue = row.tryGetScalarValue(labelSig);
            final var priceValue = row.tryGetScalarValue(priceSig);
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
