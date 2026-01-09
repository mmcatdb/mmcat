package cz.matfyz.tests.evolution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

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
import cz.matfyz.tests.example.basic.PostgreSQL;
import cz.matfyz.tests.example.basic.Schema;

/**
 * Tests for the CreateMorphism evolution operation.
 * Tests that creating a new morphism in the schema correctly creates an empty
 * instance morphism.
 */
class CreateMorphismTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    @Test
    void createMorphismBetweenExistingObjexes() {
        final var schema = Schema.newSchema();

        // Create two new objexes
        final var objex1Key = new Key(1000);
        final var objex2Key = new Key(1001);

        final var objex1 = new SerializedObjex(objex1Key, ObjexIds.fromIds(SignatureId.empty()));
        final var objex2 = new SerializedObjex(objex2Key, ObjexIds.fromIds(SignatureId.empty()));

        final var metadata1 = new SerializedMetadataObjex(objex1Key, "Entity1", null);
        final var metadata2 = new SerializedMetadataObjex(objex2Key, "Entity2", null);

        final var createObjex1 = new CreateObjex(objex1, metadata1);
        final var createObjex2 = new CreateObjex(objex2, metadata2);

        // Create morphism between them
        final var morphismSignature = new BaseSignature(500);
        final var serializedMorphism = new SerializedMorphism(
                morphismSignature,
                objex1Key,
                objex2Key,
                Min.ZERO,
                Set.of());

        final var morphismMetadata = new SerializedMetadataMorphism(
                morphismSignature,
                "newMorphism");

        final var createMorphism = new CreateMorphism(serializedMorphism, morphismMetadata);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .operation(createObjex1)
                .operation(createObjex2)
                .operation(createMorphism)
                .expectedInstance(builder -> {
                    // Orders remain, new objexes are empty, morphism exists but has no mappings
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .run();
    }

    @Test
    void createMorphismInEmptyInstance() {
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
                "newMorphism");

        final var createMorphism = new CreateMorphism(serializedMorphism, morphismMetadata);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    // Empty initial instance
                })
                .operation(createObjex1)
                .operation(createObjex2)
                .operation(createMorphism)
                .expectedInstance(builder -> {
                    // Still empty - objexes and morphism exist but have no data
                })
                .run();
    }
}
