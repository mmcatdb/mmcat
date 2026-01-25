package cz.matfyz.tests.evolution;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

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
import cz.matfyz.evolution.category.SplitProperty;
import cz.matfyz.evolution.instance.InstanceEvolver;
import cz.matfyz.tests.example.basic.Schema;

/**
 * Tests for the SPLIT operation.
 * SPLIT divides one property into multiple using a transformation function (delimiter).
 */
class SplitPropertyTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    /**
     * Helper to create a source property specification.
     */
    private SplitProperty.SourceProperty createSourceProperty(Key parentKey, int sig, int propertyKeyInt, String label) {
        final var signature = new BaseSignature(sig);
        final var propertyKey = new Key(propertyKeyInt);
        return new SplitProperty.SourceProperty(
            new SerializedObjex(propertyKey, ObjexIds.fromIds(SignatureId.empty())),
            new SerializedMetadataObjex(propertyKey, label, null),
            new SerializedMorphism(signature, parentKey, propertyKey, Min.ZERO, java.util.Set.of()),
            new SerializedMetadataMorphism(signature, label)
        );
    }

    /**
     * Helper to create a target property specification.
     */
    private SplitProperty.TargetProperty createTargetProperty(Key parentKey, int sig, int propertyKeyInt, String label) {
        final var signature = new BaseSignature(sig);
        final var propertyKey = new Key(propertyKeyInt);
        return new SplitProperty.TargetProperty(
            new SerializedObjex(propertyKey, ObjexIds.fromIds(SignatureId.empty())),
            new SerializedMetadataObjex(propertyKey, label, null),
            new SerializedMorphism(signature, parentKey, propertyKey, Min.ZERO, java.util.Set.of()),
            new SerializedMetadataMorphism(signature, label)
        );
    }

    /**
     * Test SPLIT schema changes - creates target properties.
     */
    @Test
    void splitCreatesTargetProperties() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Split product label into parts (simulating address split)
        final var splitOperation = new SplitProperty(
            Schema.product.key(),
            createSourceProperty(Schema.product.key(), 55, 55, "label"),
            List.of(
                createTargetProperty(Schema.product.key(), 6001, 6001, "part1"),
                createTargetProperty(Schema.product.key(), 6002, 6002, "part2"),
                createTargetProperty(Schema.product.key(), 6003, 6003, "part3")
            ),
            ",",
            false  // don't delete source
        );

        splitOperation.up(schema, metadata);

        // Verify: target properties created
        assertNotNull(schema.getMorphism(new BaseSignature(6001)));
        assertNotNull(schema.getMorphism(new BaseSignature(6002)));
        assertNotNull(schema.getMorphism(new BaseSignature(6003)));

        // Verify: source property still exists
        assertNotNull(schema.getMorphism(new BaseSignature(55)));
    }

    /**
     * Test SPLIT with delete source property.
     */
    @Test
    void splitDeletesSourceProperty() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        final var splitOperation = new SplitProperty(
            Schema.product.key(),
            createSourceProperty(Schema.product.key(), 55, 55, "label"),
            List.of(
                createTargetProperty(Schema.product.key(), 6001, 6001, "part1"),
                createTargetProperty(Schema.product.key(), 6002, 6002, "part2")
            ),
            ",",
            true  // delete source
        );

        splitOperation.up(schema, metadata);

        // Verify: target properties created
        assertNotNull(schema.getMorphism(new BaseSignature(6001)));
        assertNotNull(schema.getMorphism(new BaseSignature(6002)));

        // Verify: source property deleted
        assertThrows(MorphismNotFoundException.class, () -> schema.getMorphism(new BaseSignature(55)));
    }

    /**
     * Test SPLIT down() reverts the operation.
     */
    @Test
    void splitDownReverts() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        final var splitOperation = new SplitProperty(
            Schema.product.key(),
            createSourceProperty(Schema.product.key(), 55, 55, "label"),
            List.of(
                createTargetProperty(Schema.product.key(), 6001, 6001, "part1"),
                createTargetProperty(Schema.product.key(), 6002, 6002, "part2")
            ),
            ",",
            true
        );

        splitOperation.up(schema, metadata);
        splitOperation.down(schema, metadata);

        // Verify: source property restored
        assertNotNull(schema.getMorphism(new BaseSignature(55)));

        // Verify: target properties removed
        assertThrows(MorphismNotFoundException.class, () -> schema.getMorphism(new BaseSignature(6001)));
        assertThrows(MorphismNotFoundException.class, () -> schema.getMorphism(new BaseSignature(6002)));
    }

    /**
     * Test SPLIT instance data - value is split by delimiter.
     */
    @Test
    void splitDividesValue() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Build instance with products having comma-separated values
        final var builder = new InstanceBuilder(schema);
        builder
            .value(Schema.product_id, "p_1")
            .value(Schema.product_label, "street1,123,12345")  // address-like value
            .objex(Schema.product);
        builder
            .value(Schema.product_id, "p_2")
            .value(Schema.product_label, "street2,456,67890")
            .objex(Schema.product);
        final InstanceCategory instance = builder.build();

        final var splitOperation = new SplitProperty(
            Schema.product.key(),
            createSourceProperty(Schema.product.key(), 55, 55, "address"),
            List.of(
                createTargetProperty(Schema.product.key(), 6001, 6001, "street"),
                createTargetProperty(Schema.product.key(), 6002, 6002, "number"),
                createTargetProperty(Schema.product.key(), 6003, 6003, "postcode")
            ),
            ",",
            false
        );

        // Apply schema change
        splitOperation.up(schema, metadata);

        // Apply instance change
        final var evolver = new InstanceEvolver(instance);
        splitOperation.accept(evolver);

        // Verify: split values
        final var streetSig = new BaseSignature(6001);
        final var numberSig = new BaseSignature(6002);
        final var postcodeSig = new BaseSignature(6003);

        final var productObjex = instance.getObjex(Schema.product.key());
        final var streets = new java.util.ArrayList<String>();
        final var numbers = new java.util.ArrayList<String>();
        final var postcodes = new java.util.ArrayList<String>();

        for (final var row : productObjex.allRows()) {
            final var street = row.tryGetScalarValue(streetSig);
            final var number = row.tryGetScalarValue(numberSig);
            final var postcode = row.tryGetScalarValue(postcodeSig);
            if (street != null) streets.add(street);
            if (number != null) numbers.add(number);
            if (postcode != null) postcodes.add(postcode);
        }

        assertEquals(2, streets.size());
        assertEquals(2, numbers.size());
        assertEquals(2, postcodes.size());

        assertTrue(streets.contains("street1"));
        assertTrue(streets.contains("street2"));
        assertTrue(numbers.contains("123"));
        assertTrue(numbers.contains("456"));
        assertTrue(postcodes.contains("12345"));
        assertTrue(postcodes.contains("67890"));
    }

    /**
     * Test SPLIT with space delimiter (fullName -> firstName, lastName).
     */
    @Test
    void splitWithSpaceDelimiter() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Build instance with full names
        final var builder = new InstanceBuilder(schema);
        builder.value(Schema.order_number, "John Doe").objex(Schema.order);
        builder.value(Schema.order_number, "Jane Smith").objex(Schema.order);
        final InstanceCategory instance = builder.build();

        final var splitOperation = new SplitProperty(
            Schema.order.key(),
            createSourceProperty(Schema.order.key(), 1, 2, "fullName"),
            List.of(
                createTargetProperty(Schema.order.key(), 7001, 7001, "firstName"),
                createTargetProperty(Schema.order.key(), 7002, 7002, "lastName")
            ),
            " ",  // space delimiter
            false
        );

        splitOperation.up(schema, metadata);
        final var evolver = new InstanceEvolver(instance);
        splitOperation.accept(evolver);

        // Verify: split names
        final var firstNameSig = new BaseSignature(7001);
        final var lastNameSig = new BaseSignature(7002);

        final var orderObjex = instance.getObjex(Schema.order.key());
        final var firstNames = new java.util.ArrayList<String>();
        final var lastNames = new java.util.ArrayList<String>();

        for (final var row : orderObjex.allRows()) {
            final var firstName = row.tryGetScalarValue(firstNameSig);
            final var lastName = row.tryGetScalarValue(lastNameSig);
            if (firstName != null) firstNames.add(firstName);
            if (lastName != null) lastNames.add(lastName);
        }

        assertEquals(2, firstNames.size());
        assertEquals(2, lastNames.size());
        assertTrue(firstNames.contains("John"));
        assertTrue(firstNames.contains("Jane"));
        assertTrue(lastNames.contains("Doe"));
        assertTrue(lastNames.contains("Smith"));
    }

}
