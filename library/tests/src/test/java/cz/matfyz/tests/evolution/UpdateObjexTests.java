package cz.matfyz.tests.evolution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObjex;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;
import cz.matfyz.core.utils.UniqueIdGenerator;
import cz.matfyz.evolution.category.CreateObjex;
import cz.matfyz.evolution.category.UpdateObjex;
import cz.matfyz.tests.example.basic.PostgreSQL;
import cz.matfyz.tests.example.basic.Schema;

/**
 * Tests for the UpdateObjex evolution operation.
 * Tests that updating an objex in the schema correctly handles metadata changes
 * without affecting the instance data.
 */
class UpdateObjexTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    @Test
    void updateObjexIds() {
        final var schema = Schema.newSchema();

        // Create a new objex
        final var objexKey = new Key(1000);
        final var originalObjex = new SerializedObjex(
                objexKey,
                ObjexIds.fromIds(SignatureId.empty()));

        final var originalMetadata = new SerializedMetadataObjex(
                objexKey,
                "OriginalEntity",
                null);

        final var createOperation = new CreateObjex(originalObjex, originalMetadata);

        // Update the objex with new ids
        final var updatedObjex = new SerializedObjex(
                objexKey,
                ObjexIds.fromIds(SignatureId.empty())); // Same ids, but could be different

        final var updateOperation = new UpdateObjex(updatedObjex, originalObjex);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .operation(createOperation)
                .operation(updateOperation)
                .expectedInstance(builder -> {
                    // Orders remain unchanged, objex metadata updated
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .run();
    }

    @Test
    void updateObjexWithEmptyInstance() {
        final var schema = Schema.newSchema();

        final var objexKey = new Key(1000);
        final var originalObjex = new SerializedObjex(
                objexKey,
                ObjexIds.fromIds(SignatureId.empty()));

        final var originalMetadata = new SerializedMetadataObjex(
                objexKey,
                "OriginalEntity",
                null);

        final var createOperation = new CreateObjex(originalObjex, originalMetadata);

        final var updatedObjex = new SerializedObjex(
                objexKey,
                ObjexIds.fromIds(SignatureId.empty()));

        final var updateOperation = new UpdateObjex(updatedObjex, originalObjex);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    // Empty instance
                })
                .operation(createOperation)
                .operation(updateOperation)
                .expectedInstance(builder -> {
                    // Still empty, just schema metadata changed
                })
                .run();
    }

    @Test
    void updateObjexPreservesData() {
        final var schema = Schema.newSchema();

        final var objexKey = new Key(1000);
        final var originalObjex = new SerializedObjex(
                objexKey,
                ObjexIds.fromIds(SignatureId.empty()));

        final var originalMetadata = new SerializedMetadataObjex(
                objexKey,
                "EntityV1",
                null);

        final var createOperation = new CreateObjex(originalObjex, originalMetadata);

        // Update to a "new version"
        final var updatedObjex = new SerializedObjex(
                objexKey,
                ObjexIds.fromIds(SignatureId.empty()));

        final var updateOperation = new UpdateObjex(updatedObjex, originalObjex);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    PostgreSQL.addOrder(builder, "o_100");
                    PostgreSQL.addOrder(builder, "o_200");
                    PostgreSQL.addProduct(builder, "p_1", "Product 1", "10.00");
                })
                .operation(createOperation)
                .operation(updateOperation)
                .expectedInstance(builder -> {
                    // All data remains unchanged
                    PostgreSQL.addOrder(builder, "o_100");
                    PostgreSQL.addOrder(builder, "o_200");
                    PostgreSQL.addProduct(builder, "p_1", "Product 1", "10.00");
                })
                .run();
    }
}
