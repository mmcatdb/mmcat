package cz.matfyz.tests.evolution;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataMorphism;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObjex;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;
import cz.matfyz.core.utils.UniqueIdGenerator;
import cz.matfyz.evolution.category.CreateMorphism;
import cz.matfyz.evolution.category.CreateObjex;
import cz.matfyz.evolution.category.UpdateMorphism;
import cz.matfyz.tests.example.basic.PostgreSQL;
import cz.matfyz.tests.example.basic.Schema;

/**
 * Tests for the UpdateMorphism evolution operation.
 * Tests that updating a morphism in the schema correctly handles cardinality
 * and other metadata changes without affecting existing mapping data.
 */
class UpdateMorphismTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    @Test
    void updateMorphismCardinality() {
        final var schema = Schema.newSchema();

        // Create objexes
        final var objex1Key = new Key(1000);
        final var objex2Key = new Key(1001);

        final var objex1 = new SerializedObjex(objex1Key, ObjexIds.fromIds(SignatureId.empty()));
        final var objex2 = new SerializedObjex(objex2Key, ObjexIds.fromIds(SignatureId.empty()));

        final var metadata1 = new SerializedMetadataObjex(objex1Key, "Entity1", null);
        final var metadata2 = new SerializedMetadataObjex(objex2Key, "Entity2", null);

        final var createObjex1 = new CreateObjex(objex1, metadata1);
        final var createObjex2 = new CreateObjex(objex2, metadata2);

        // Create morphism with Min.ZERO
        final var morphismSignature = new BaseSignature(500);
        final var originalMorphism = new SerializedMorphism(
                morphismSignature,
                objex1Key,
                objex2Key,
                Min.ZERO,
                Set.of());

        final var morphismMetadata = new SerializedMetadataMorphism(
                morphismSignature,
                "myMorphism");

        final var createMorphism = new CreateMorphism(originalMorphism, morphismMetadata);

        // Update to Min.ONE
        final var updatedMorphism = new SerializedMorphism(
                morphismSignature,
                objex1Key,
                objex2Key,
                Min.ONE,
                Set.of());

        final var updateMorphism = new UpdateMorphism(updatedMorphism, originalMorphism);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .operation(createObjex1)
                .operation(createObjex2)
                .operation(createMorphism)
                .operation(updateMorphism)
                .expectedInstance(builder -> {
                    // Data unchanged, only morphism metadata updated
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .run();
    }

    @Test
    void updateMorphismWithEmptyInstance() {
        final var schema = Schema.newSchema();

        final var objex1Key = new Key(1000);
        final var objex2Key = new Key(1001);

        final var objex1 = new SerializedObjex(objex1Key, ObjexIds.fromIds(SignatureId.empty()));
        final var objex2 = new SerializedObjex(objex2Key, ObjexIds.fromIds(SignatureId.empty()));

        final var metadata1 = new SerializedMetadataObjex(objex1Key, "Entity1", null);
        final var metadata2 = new SerializedMetadataObjex(objex2Key, "Entity2", null);

        final var createObjex1 = new CreateObjex(objex1, metadata1);
        final var createObjex2 = new CreateObjex(objex2, metadata2);

        final var morphismSignature = new BaseSignature(500);
        final var originalMorphism = new SerializedMorphism(
                morphismSignature,
                objex1Key,
                objex2Key,
                Min.ZERO,
                Set.of());

        final var morphismMetadata = new SerializedMetadataMorphism(
                morphismSignature,
                "myMorphism");

        final var createMorphism = new CreateMorphism(originalMorphism, morphismMetadata);

        final var updatedMorphism = new SerializedMorphism(
                morphismSignature,
                objex1Key,
                objex2Key,
                Min.ONE,
                Set.of());

        final var updateMorphism = new UpdateMorphism(updatedMorphism, originalMorphism);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    // Empty instance
                })
                .operation(createObjex1)
                .operation(createObjex2)
                .operation(createMorphism)
                .operation(updateMorphism)
                .expectedInstance(builder -> {
                    // Still empty
                })
                .run();
    }

    @Test
    void updateMorphismPreservesData() {
        final var schema = Schema.newSchema();

        final var objex1Key = new Key(1000);
        final var objex2Key = new Key(1001);

        final var objex1 = new SerializedObjex(objex1Key, ObjexIds.fromIds(SignatureId.empty()));
        final var objex2 = new SerializedObjex(objex2Key, ObjexIds.fromIds(SignatureId.empty()));

        final var metadata1 = new SerializedMetadataObjex(objex1Key, "Entity1", null);
        final var metadata2 = new SerializedMetadataObjex(objex2Key, "Entity2", null);

        final var createObjex1 = new CreateObjex(objex1, metadata1);
        final var createObjex2 = new CreateObjex(objex2, metadata2);

        final var morphismSignature = new BaseSignature(500);
        final var originalMorphism = new SerializedMorphism(
                morphismSignature,
                objex1Key,
                objex2Key,
                Min.ZERO,
                Set.of());

        final var morphismMetadata = new SerializedMetadataMorphism(
                morphismSignature,
                "myMorphism");

        final var createMorphism = new CreateMorphism(originalMorphism, morphismMetadata);

        final var updatedMorphism = new SerializedMorphism(
                morphismSignature,
                objex1Key,
                objex2Key,
                Min.ONE,
                Set.of());

        final var updateMorphism = new UpdateMorphism(updatedMorphism, originalMorphism);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    PostgreSQL.addOrder(builder, "o_100");
                    PostgreSQL.addOrder(builder, "o_200");
                    PostgreSQL.addProduct(builder, "p_1", "Widget", "19.99");
                })
                .operation(createObjex1)
                .operation(createObjex2)
                .operation(createMorphism)
                .operation(updateMorphism)
                .expectedInstance(builder -> {
                    // All existing data preserved
                    PostgreSQL.addOrder(builder, "o_100");
                    PostgreSQL.addOrder(builder, "o_200");
                    PostgreSQL.addProduct(builder, "p_1", "Widget", "19.99");
                })
                .run();
    }
}
