package cz.matfyz.tests.evolution;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import cz.matfyz.evolution.category.GroupProperties.MovedMorphism;
import cz.matfyz.evolution.instance.InstanceEvolver;
import cz.matfyz.tests.example.basic.Schema;

/**
 * Tests for the GROUP operation which groups multiple properties under a new intermediate object.
 *
 * Example transformation:
 * Before: Customer.firstName, Customer.lastName
 * After: Customer.name.firstName, Customer.name.lastName
 */
class GroupPropertiesTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

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

            return new MovedMorphism(oldMorphism, oldMetadata, newMorphism, newMetadata);
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

    record MovedMorphismSpec(int oldSig, int newSig, int propertyKey, String label) {}

    @Test
    void groupSchemaChangesApplyCorrectly() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Group: Order.number -> Order.details.number
        // Old morphism: Order (key=1) -> number (key=2) via signature 1
        // New structure: Order -> details (key=1000) -> number via new signatures
        final var groupOperation = createGroupOperation(
            Schema.order.key(),           // Parent: Order (key=1)
            1000,                         // New grouping objex key
            "details",                    // Grouping label
            1001,                         // Parent -> grouping morphism signature
            List.of(
                new MovedMorphismSpec(1, 1002, 2, "number")  // Move order_number (key 2, not 3!)
            )
        );

        // Apply schema changes
        groupOperation.up(schema, metadata);

        // Verify: new grouping objex exists
        final var groupingObjex = schema.getObjex(new Key(1000));
        assertNotNull(groupingObjex, "Grouping objex should exist after GROUP");

        // Verify: morphism from parent to grouping exists
        final var parentToGroup = schema.getMorphism(new BaseSignature(1001));
        assertNotNull(parentToGroup, "Parent to grouping morphism should exist");
        assertEquals(Schema.order.key(), parentToGroup.dom().key());
        assertEquals(new Key(1000), parentToGroup.cod().key());

        // Verify: new morphism from grouping to property exists
        final var groupToProperty = schema.getMorphism(new BaseSignature(1002));
        assertNotNull(groupToProperty, "Grouping to property morphism should exist");
        assertEquals(new Key(1000), groupToProperty.dom().key());
        assertEquals(new Key(2), groupToProperty.cod().key());  // number key
    }

    @Test
    void groupSchemaDownRevertsChanges() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        final var groupOperation = createGroupOperation(
            Schema.order.key(),
            1000,
            "details",
            1001,
            List.of(
                new MovedMorphismSpec(1, 1002, 2, "number")  // number has key 2
            )
        );

        // Apply then revert
        groupOperation.up(schema, metadata);
        groupOperation.down(schema, metadata);

        // Verify: grouping objex is removed
        final var groupingObjex = schema.getObjex(new Key(1000));
        assertNull(groupingObjex, "Grouping objex should be removed after down()");

        // Verify: original morphism is restored
        final var originalMorphism = schema.getMorphism(new BaseSignature(1));
        assertNotNull(originalMorphism, "Original morphism should be restored after down()");
    }

    @Test
    void groupInstanceMigratesData() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Build initial instance with data
        final var builder = new InstanceBuilder(schema);
        builder.value(Schema.order_number, "o_100").objex(Schema.order);
        builder.value(Schema.order_number, "o_200").objex(Schema.order);
        final InstanceCategory instance = builder.build();

        // Verify initial state
        final var orderObjex = instance.getObjex(Schema.order.key());
        assertNotNull(orderObjex);
        assertEquals(2, countRows(orderObjex));

        // Create and apply GROUP operation
        final var groupOperation = createGroupOperation(
            Schema.order.key(),
            1000,
            "details",
            1001,
            List.of(
                new MovedMorphismSpec(1, 1002, 2, "number")  // number has key 2
            )
        );

        // Apply schema changes first
        groupOperation.up(schema, metadata);

        // Apply instance changes
        final var evolver = new InstanceEvolver(instance);
        groupOperation.accept(evolver);

        // Verify: grouping objex was created with rows
        final var groupingObjex = instance.getObjex(new Key(1000));
        assertNotNull(groupingObjex, "Grouping objex should exist in instance");
        assertEquals(2, countRows(groupingObjex), "Should have 2 rows in grouping objex (one per order)");

        // Verify: parent to group morphism has mappings
        final var parentToGroupMorphism = instance.getMorphism(new BaseSignature(1001));
        assertNotNull(parentToGroupMorphism);
        assertFalse(parentToGroupMorphism.isEmpty(), "Parent to group morphism should have mappings");

        // Verify: new morphism from grouping to property exists
        final var groupToPropertyMorphism = instance.getMorphism(new BaseSignature(1002));
        assertNotNull(groupToPropertyMorphism, "Group to property morphism should exist");

        // For non-entity properties, values are stored in DomainRow's propertyValues, not as MappingRows
        // Verify that the property values were migrated to the grouping rows
        final var newSig = new BaseSignature(1002);
        final var groupingValues = new java.util.ArrayList<String>();
        for (final var row : groupingObjex.allRows()) {
            final var value = row.tryGetScalarValue(newSig);
            if (value != null) {
                groupingValues.add(value);
            }
        }
        assertEquals(2, groupingValues.size(), "Should have 2 property values migrated to grouping rows");
        assertTrue(groupingValues.contains("o_100"), "Should contain value o_100");
        assertTrue(groupingValues.contains("o_200"), "Should contain value o_200");
    }

    @Test
    void groupWithMultipleProperties() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Build initial instance with product data (id, label, price)
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

        // Group label and price under a "details" object
        // Product.label, Product.price -> Product.details.label, Product.details.price
        // product_label morphism: signature 55, product -> label (key 55)
        // product_price morphism: signature 56, product -> price (key 56)
        final var groupOperation = createGroupOperation(
            Schema.product.key(),         // Parent: Product (key=52)
            2000,                         // New grouping objex key
            "productDetails",             // Grouping label
            2001,                         // Parent -> grouping morphism signature
            List.of(
                new MovedMorphismSpec(55, 2002, 55, "label"),  // Move product_label (sig 55, label key 55)
                new MovedMorphismSpec(56, 2003, 56, "price")   // Move product_price (sig 56, price key 56)
            )
        );

        // Apply changes
        groupOperation.up(schema, metadata);
        final var evolver = new InstanceEvolver(instance);
        groupOperation.accept(evolver);

        // Verify: grouping objex created with 2 rows
        final var groupingObjex = instance.getObjex(new Key(2000));
        assertNotNull(groupingObjex, "Grouping objex should exist");
        assertEquals(2, countRows(groupingObjex), "Should have 2 rows (one per product)");

        // Verify: both moved morphisms exist
        final var labelMorphism = instance.getMorphism(new BaseSignature(2002));
        final var priceMorphism = instance.getMorphism(new BaseSignature(2003));
        assertNotNull(labelMorphism, "Label morphism should exist");
        assertNotNull(priceMorphism, "Price morphism should exist");

        // For non-entity properties, verify values were migrated to grouping rows
        final var labelSig = new BaseSignature(2002);
        final var priceSig = new BaseSignature(2003);
        final var labelValues = new java.util.ArrayList<String>();
        final var priceValues = new java.util.ArrayList<String>();

        for (final var row : groupingObjex.allRows()) {
            final var labelValue = row.tryGetScalarValue(labelSig);
            final var priceValue = row.tryGetScalarValue(priceSig);
            if (labelValue != null) labelValues.add(labelValue);
            if (priceValue != null) priceValues.add(priceValue);
        }

        assertEquals(2, labelValues.size(), "Should have 2 label values migrated");
        assertEquals(2, priceValues.size(), "Should have 2 price values migrated");
        assertTrue(labelValues.contains("Laptop"), "Should contain label Laptop");
        assertTrue(labelValues.contains("Mouse"), "Should contain label Mouse");
        assertTrue(priceValues.contains("999.99"), "Should contain price 999.99");
        assertTrue(priceValues.contains("29.99"), "Should contain price 29.99");
    }

    private int countRows(cz.matfyz.core.instance.InstanceObjex objex) {
        int count = 0;
        for (var row : objex.allRows()) {
            count++;
        }
        return count;
    }
}
