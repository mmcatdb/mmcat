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
import cz.matfyz.tests.example.basic.PostgreSQL;
import cz.matfyz.tests.example.basic.Schema;

/**
 * Tests for the CreateObjex evolution operation.
 * Tests that creating a new objex in the schema correctly creates an empty
 * objex in the instance.
 */
class CreateObjexTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    @Test
    void createObjexWithEmptyInstance() {
        final var schema = Schema.newSchema();

        // Create a new entity objex
        final var newKey = new Key(1000);
        final var serializedObjex = new SerializedObjex(
                newKey,
                ObjexIds.fromIds(SignatureId.empty()));

        final var serializedMetadata = new SerializedMetadataObjex(
                newKey,
                "NewEntity",
                null);

        final var createOperation = new CreateObjex(serializedObjex, serializedMetadata);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    // Empty initial instance
                })
                .operation(createOperation)
                .expectedInstance(builder -> {
                    // Expected: still empty, but now has the new objex
                })
                .run();
    }

    @Test
    void createObjexWithExistingData() {
        final var schema = Schema.newSchema();

        // Create a new entity objex
        final var newKey = new Key(1000);
        final var serializedObjex = new SerializedObjex(
                newKey,
                ObjexIds.fromIds(SignatureId.empty()));

        final var serializedMetadata = new SerializedMetadataObjex(
                newKey,
                "NewEntity",
                null);

        final var createOperation = new CreateObjex(serializedObjex, serializedMetadata);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    // Initial instance with some orders
                    PostgreSQL.addOrder(builder, "o_100");
                    PostgreSQL.addOrder(builder, "o_200");
                })
                .operation(createOperation)
                .expectedInstance(builder -> {
                    // Expected: same orders exist, new objex is empty
                    PostgreSQL.addOrder(builder, "o_100");
                    PostgreSQL.addOrder(builder, "o_200");
                    // The new objex exists but is empty (no rows added)
                })
                .run();
    }

}
