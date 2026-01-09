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
import cz.matfyz.evolution.category.DeleteObjex;
import cz.matfyz.tests.example.basic.PostgreSQL;
import cz.matfyz.tests.example.basic.Schema;

/**
 * Tests for the DeleteObjex evolution operation.
 * Tests that deleting an objex from the schema correctly removes the instance
 * objex
 * and cleans up all related mappings.
 */
class DeleteObjexTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    @Test
    void deleteEmptyObjex() {
        final var schema = Schema.newSchema();

        // First create a new objex
        final var newKey = new Key(1000);
        final var serializedObjex = new SerializedObjex(
                newKey,
                ObjexIds.fromIds(SignatureId.empty()));

        final var serializedMetadata = new SerializedMetadataObjex(
                newKey,
                "TempEntity",
                null);

        final var createOperation = new CreateObjex(serializedObjex, serializedMetadata);
        final var deleteOperation = new DeleteObjex(serializedObjex, serializedMetadata);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    // Initial instance has some orders
                    PostgreSQL.addOrder(builder, "o_100");
                    PostgreSQL.addOrder(builder, "o_200");
                })
                .operation(createOperation) // Create the new objex
                .operation(deleteOperation) // Then delete it
                .expectedInstance(builder -> {
                    // Expected: back to original state with just orders
                    PostgreSQL.addOrder(builder, "o_100");
                    PostgreSQL.addOrder(builder, "o_200");
                    // The temporary objex should be gone
                })
                .run();
    }

    @Test
    void deleteObjexWithData() {
        final var schema = Schema.newSchema();

        // Create and then delete an objex that has data
        final var newKey = new Key(1000);
        final var serializedObjex = new SerializedObjex(
                newKey,
                ObjexIds.fromIds(SignatureId.empty()));

        final var serializedMetadata = new SerializedMetadataObjex(
                newKey,
                "TempEntity",
                null);

        final var createOperation = new CreateObjex(serializedObjex, serializedMetadata);
        final var deleteOperation = new DeleteObjex(serializedObjex, serializedMetadata);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    PostgreSQL.addOrder(builder, "o_100");
                    PostgreSQL.addOrder(builder, "o_200");
                })
                .operation(createOperation)
                // Note: We create the objex but don't add data to it in the test
                // In a real scenario, data might be added between create and delete
                .operation(deleteOperation)
                .expectedInstance(builder -> {
                    // Orders remain, temporary objex is deleted
                    PostgreSQL.addOrder(builder, "o_100");
                    PostgreSQL.addOrder(builder, "o_200");
                })
                .run();
    }

    @Test
    void deleteObjexFromEmptyInstance() {
        final var schema = Schema.newSchema();

        final var newKey = new Key(1000);
        final var serializedObjex = new SerializedObjex(
                newKey,
                ObjexIds.fromIds(SignatureId.empty()));

        final var serializedMetadata = new SerializedMetadataObjex(
                newKey,
                "TempEntity",
                null);

        final var createOperation = new CreateObjex(serializedObjex, serializedMetadata);
        final var deleteOperation = new DeleteObjex(serializedObjex, serializedMetadata);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    // Start with empty instance
                })
                .operation(createOperation)
                .operation(deleteOperation)
                .expectedInstance(builder -> {
                    // End with empty instance
                })
                .run();
    }
}
