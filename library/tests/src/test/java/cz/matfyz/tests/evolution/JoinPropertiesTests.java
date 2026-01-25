package cz.matfyz.tests.evolution;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;

import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.SignatureId;
import org.junit.jupiter.api.Test;

import cz.matfyz.core.exception.MorphismNotFoundException;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
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
import cz.matfyz.evolution.category.JoinProperties;
import cz.matfyz.evolution.instance.InstanceEvolver;
import cz.matfyz.tests.example.basic.Schema;

/**
 * Tests for the JOIN operation.
 * JOIN combines multiple properties into one using a transformation function (delimiter).
 */
class JoinPropertiesTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    /**
     * Helper to create a source property specification.
     */
    private JoinProperties.SourceProperty createSourceProperty(Key parentKey, int sig, int propertyKeyInt, String label) {
        final var signature = new BaseSignature(sig);
        final var propertyKey = new Key(propertyKeyInt);
        return new JoinProperties.SourceProperty(
            new SerializedObjex(propertyKey, ObjexIds.fromIds(SignatureId.empty())),
            new SerializedMetadataObjex(propertyKey, label, null),
            new SerializedMorphism(signature, parentKey, propertyKey, Min.ZERO, java.util.Set.of()),
            new SerializedMetadataMorphism(signature, label)
        );
    }

    /**
     * Helper to create a target property specification.
     */
    private JoinProperties.TargetProperty createTargetProperty(Key parentKey, int sig, int propertyKeyInt, String label) {
        final var signature = new BaseSignature(sig);
        final var propertyKey = new Key(propertyKeyInt);
        return new JoinProperties.TargetProperty(
            new SerializedObjex(propertyKey, ObjexIds.fromIds(SignatureId.empty())),
            new SerializedMetadataObjex(propertyKey, label, null),
            new SerializedMorphism(signature, parentKey, propertyKey, Min.ZERO, java.util.Set.of()),
            new SerializedMetadataMorphism(signature, label)
        );
    }

    /**
     * Test JOIN schema changes - creates target property, optionally deletes sources.
     */
    @Test
    void joinCreatesTargetProperty() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Join product label and price into a "description" property
        // product_label: sig 55, product_price: sig 56
        final var joinOperation = new JoinProperties(
            Schema.product.key(),
            List.of(
                createSourceProperty(Schema.product.key(), 55, 55, "label"),
                createSourceProperty(Schema.product.key(), 56, 56, "price")
            ),
            createTargetProperty(Schema.product.key(), 5001, 5001, "description"),
            " - $",
            false  // don't delete source properties
        );

        joinOperation.up(schema, metadata);

        // Verify: target property created
        assertNotNull(schema.getMorphism(new BaseSignature(5001)), "Target morphism should be created");
        assertNotNull(schema.getObjex(new Key(5001)), "Target objex should be created");

        // Verify: source properties still exist
        assertNotNull(schema.getMorphism(new BaseSignature(55)), "Source label morphism should still exist");
        assertNotNull(schema.getMorphism(new BaseSignature(56)), "Source price morphism should still exist");
    }

    /**
     * Test JOIN with delete source properties.
     */
    @Test
    void joinDeletesSourceProperties() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        final var joinOperation = new JoinProperties(
            Schema.product.key(),
            List.of(
                createSourceProperty(Schema.product.key(), 55, 55, "label"),
                createSourceProperty(Schema.product.key(), 56, 56, "price")
            ),
            createTargetProperty(Schema.product.key(), 5001, 5001, "description"),
            " - $",
            true  // delete source properties
        );

        joinOperation.up(schema, metadata);

        // Verify: target property created
        assertNotNull(schema.getMorphism(new BaseSignature(5001)));

        // Verify: source properties deleted
        assertThrows(MorphismNotFoundException.class, () -> schema.getMorphism(new BaseSignature(55)));
        assertThrows(MorphismNotFoundException.class, () -> schema.getMorphism(new BaseSignature(56)));
    }

    /**
     * Test JOIN down() reverts the operation.
     */
    @Test
    void joinDownReverts() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        final var joinOperation = new JoinProperties(
            Schema.product.key(),
            List.of(
                createSourceProperty(Schema.product.key(), 55, 55, "label"),
                createSourceProperty(Schema.product.key(), 56, 56, "price")
            ),
            createTargetProperty(Schema.product.key(), 5001, 5001, "description"),
            " - $",
            true
        );

        joinOperation.up(schema, metadata);
        joinOperation.down(schema, metadata);

        // Verify: source properties restored
        assertNotNull(schema.getMorphism(new BaseSignature(55)));
        assertNotNull(schema.getMorphism(new BaseSignature(56)));

        // Verify: target property removed
        assertThrows(MorphismNotFoundException.class, () -> schema.getMorphism(new BaseSignature(5001)));
    }

    /**
     * Test JOIN instance data - values are concatenated with delimiter.
     */
    @Test
    void joinConcatenatesValues() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Build instance with products
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

        final var joinOperation = new JoinProperties(
            Schema.product.key(),
            List.of(
                createSourceProperty(Schema.product.key(), 55, 55, "label"),
                createSourceProperty(Schema.product.key(), 56, 56, "price")
            ),
            createTargetProperty(Schema.product.key(), 5001, 5001, "description"),
            " - $",
            false
        );

        // Apply schema change
        joinOperation.up(schema, metadata);

        // Apply instance change
        final var evolver = new InstanceEvolver(instance);
        joinOperation.accept(evolver);

        // Verify: joined values
        final var targetSig = new BaseSignature(5001);
        final var productObjex = instance.getObjex(Schema.product.key());
        final var values = new java.util.ArrayList<String>();

        for (final var row : productObjex.allRows()) {
            final var value = row.tryGetScalarValue(targetSig);
            if (value != null) {
                values.add(value);
            }
        }

        assertEquals(2, values.size(), "Should have 2 joined values");
        assertTrue(values.contains("Laptop - $999.99"), "Should contain joined value 'Laptop - $999.99'");
        assertTrue(values.contains("Mouse - $29.99"), "Should contain joined value 'Mouse - $29.99'");
    }

    /**
     * Test JOIN with space delimiter (firstName + lastName -> fullName).
     */
    @Test
    void joinWithSpaceDelimiter() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // We'll use order for this test (simulating firstName/lastName scenario)
        // Using order_number as first name and creating another property for last name
        final var builder = new InstanceBuilder(schema);
        builder.value(Schema.order_number, "John").objex(Schema.order);
        final InstanceCategory instance = builder.build();

        // Add a "lastName" property value manually
        final var orderObjex = instance.getObjex(Schema.order.key());
        final var lastNameSig = new BaseSignature(3002);
        for (final var row : orderObjex.allRows()) {
            row.addPropertyValue(lastNameSig, "Doe");
        }

        // Create join operation
        final var joinOperation = new JoinProperties(
            Schema.order.key(),
            List.of(
                createSourceProperty(Schema.order.key(), 1, 2, "firstName"),  // order_number as firstName
                createSourceProperty(Schema.order.key(), 3002, 3002, "lastName")
            ),
            createTargetProperty(Schema.order.key(), 5002, 5002, "fullName"),
            " ",  // space delimiter
            false
        );

        joinOperation.up(schema, metadata);
        final var evolver = new InstanceEvolver(instance);
        joinOperation.accept(evolver);

        // Verify: joined value with space
        final var targetSig = new BaseSignature(5002);
        for (final var row : orderObjex.allRows()) {
            final var value = row.tryGetScalarValue(targetSig);
            assertEquals("John Doe", value, "Should be 'John Doe'");
        }
    }

}
