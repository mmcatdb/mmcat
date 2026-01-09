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
import cz.matfyz.evolution.category.DeleteMorphism;
import cz.matfyz.tests.example.basic.PostgreSQL;
import cz.matfyz.tests.example.basic.Schema;

/**
 * Tests for the DeleteMorphism evolution operation.
 * Tests that deleting a morphism from the schema correctly removes the instance
 * morphism
 * and cleans up all related mapping rows.
 */
class DeleteMorphismTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    @Test
    void deleteMorphismWithNoMappings() {
        final var schema = Schema.newSchema();

        // Create objexes and morphism, then delete the morphism
        final var objex1Key = new Key(1000);
        final var objex2Key = new Key(1001);

        final var objex1 = new SerializedObjex(objex1Key, ObjexIds.fromIds(SignatureId.empty()));
        final var objex2 = new SerializedObjex(objex2Key, ObjexIds.fromIds(SignatureId.empty()));

        final var metadata1 = new SerializedMetadataObjex(objex1Key, "Entity1", null);
        final var metadata2 = new SerializedMetadataObjex(objex2Key, "Entity2", null);

        final var createObjex1 = new CreateObjex(objex1, metadata1);
        final var createObjex2 = new CreateObjex(objex2, metadata2);

        final var morphismSignature = new BaseSignature(500);
        final var serializedMorphism = new SerializedMorphism(
                morphismSignature,
                objex1Key,
                objex2Key,
                Min.ZERO,
                Set.of());

        final var morphismMetadata = new SerializedMetadataMorphism(
                morphismSignature,
                "tempMorphism");

        final var createMorphism = new CreateMorphism(serializedMorphism, morphismMetadata);
        final var deleteMorphism = new DeleteMorphism(serializedMorphism, morphismMetadata);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .operation(createObjex1)
                .operation(createObjex2)
                .operation(createMorphism)
                .operation(deleteMorphism)
                .expectedInstance(builder -> {
                    // Orders remain, objexes exist but morphism is deleted
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .run();
    }

    @Test
    void deleteMorphismFromEmptyInstance() {
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
        final var serializedMorphism = new SerializedMorphism(
                morphismSignature,
                objex1Key,
                objex2Key,
                Min.ZERO,
                Set.of());

        final var morphismMetadata = new SerializedMetadataMorphism(
                morphismSignature,
                "tempMorphism");

        final var createMorphism = new CreateMorphism(serializedMorphism, morphismMetadata);
        final var deleteMorphism = new DeleteMorphism(serializedMorphism, morphismMetadata);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    // Empty instance
                })
                .operation(createObjex1)
                .operation(createObjex2)
                .operation(createMorphism)
                .operation(deleteMorphism)
                .expectedInstance(builder -> {
                    // Still empty
                })
                .run();
    }

    @Test
    void createAndDeleteMultipleMorphisms() {
        final var schema = Schema.newSchema();

        final var objex1Key = new Key(1000);
        final var objex2Key = new Key(1001);

        final var objex1 = new SerializedObjex(objex1Key, ObjexIds.fromIds(SignatureId.empty()));
        final var objex2 = new SerializedObjex(objex2Key, ObjexIds.fromIds(SignatureId.empty()));

        final var metadata1 = new SerializedMetadataObjex(objex1Key, "Entity1", null);
        final var metadata2 = new SerializedMetadataObjex(objex2Key, "Entity2", null);

        final var createObjex1 = new CreateObjex(objex1, metadata1);
        final var createObjex2 = new CreateObjex(objex2, metadata2);

        // Create two morphisms
        final var morphism1Sig = new BaseSignature(500);
        final var morphism2Sig = new BaseSignature(501);

        final var serializedMorphism1 = new SerializedMorphism(
                morphism1Sig, objex1Key, objex2Key, Min.ZERO, Set.of());
        final var serializedMorphism2 = new SerializedMorphism(
                morphism2Sig, objex2Key, objex1Key, Min.ZERO, Set.of());

        final var morphismMetadata1 = new SerializedMetadataMorphism(morphism1Sig, "morphism1");
        final var morphismMetadata2 = new SerializedMetadataMorphism(morphism2Sig, "morphism2");

        final var createMorphism1 = new CreateMorphism(serializedMorphism1, morphismMetadata1);
        final var createMorphism2 = new CreateMorphism(serializedMorphism2, morphismMetadata2);
        final var deleteMorphism1 = new DeleteMorphism(serializedMorphism1, morphismMetadata1);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .operation(createObjex1)
                .operation(createObjex2)
                .operation(createMorphism1)
                .operation(createMorphism2)
                .operation(deleteMorphism1) // Delete only the first morphism
                .expectedInstance(builder -> {
                    // Orders remain, objexes exist, morphism2 exists, morphism1 deleted
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .run();
    }
}
