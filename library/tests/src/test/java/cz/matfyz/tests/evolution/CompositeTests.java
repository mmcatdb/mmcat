package cz.matfyz.tests.evolution;

import java.util.List;
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
import cz.matfyz.evolution.category.Composite;
import cz.matfyz.evolution.category.CreateMorphism;
import cz.matfyz.evolution.category.CreateObjex;
import cz.matfyz.evolution.category.DeleteMorphism;
import cz.matfyz.evolution.category.DeleteObjex;
import cz.matfyz.tests.example.basic.PostgreSQL;
import cz.matfyz.tests.example.basic.Schema;

/**
 * Tests for the Composite evolution operation.
 * Tests that composite operations correctly execute all child operations
 * in sequence and handle various combinations.
 */
class CompositeTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    @Test
    void compositeCreateObjexes() {
        final var schema = Schema.newSchema();

        // Create multiple objexes via Composite
        final var objex1Key = new Key(1000);
        final var objex2Key = new Key(1001);
        final var objex3Key = new Key(1002);

        final var objex1 = new SerializedObjex(objex1Key, ObjexIds.fromIds(SignatureId.empty()));
        final var objex2 = new SerializedObjex(objex2Key, ObjexIds.fromIds(SignatureId.empty()));
        final var objex3 = new SerializedObjex(objex3Key, ObjexIds.fromIds(SignatureId.empty()));

        final var metadata1 = new SerializedMetadataObjex(objex1Key, "Entity1", null);
        final var metadata2 = new SerializedMetadataObjex(objex2Key, "Entity2", null);
        final var metadata3 = new SerializedMetadataObjex(objex3Key, "Entity3", null);

        final var composite = new Composite("createMultiple", List.of(
                new CreateObjex(objex1, metadata1),
                new CreateObjex(objex2, metadata2),
                new CreateObjex(objex3, metadata3)));

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .operation(composite)
                .expectedInstance(builder -> {
                    // Order remains, three new empty objexes created
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .run();
    }

    @Test
    void compositeCreateAndDeleteCancellation() {
        final var schema = Schema.newSchema();

        // Create and then delete - should cancel out
        final var tempKey = new Key(1000);
        final var tempObjex = new SerializedObjex(tempKey, ObjexIds.fromIds(SignatureId.empty()));
        final var tempMetadata = new SerializedMetadataObjex(tempKey, "TempEntity", null);

        final var composite = new Composite("createAndDelete", List.of(
                new CreateObjex(tempObjex, tempMetadata),
                new DeleteObjex(tempObjex, tempMetadata)));

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    PostgreSQL.addOrder(builder, "o_100");
                    PostgreSQL.addOrder(builder, "o_200");
                })
                .operation(composite)
                .expectedInstance(builder -> {
                    // Back to original state
                    PostgreSQL.addOrder(builder, "o_100");
                    PostgreSQL.addOrder(builder, "o_200");
                })
                .run();
    }

    @Test
    void compositeCreateObjexWithMorphism() {
        final var schema = Schema.newSchema();

        // Create objex and morphism together (simulates ADD Property)
        final var entityKey = new Key(1000);
        final var propertyKey = new Key(1001);

        final var entityObjex = new SerializedObjex(entityKey, ObjexIds.fromIds(SignatureId.empty()));
        final var propertyObjex = new SerializedObjex(propertyKey, ObjexIds.fromIds(SignatureId.empty()));

        final var entityMetadata = new SerializedMetadataObjex(entityKey, "Entity", null);
        final var propertyMetadata = new SerializedMetadataObjex(propertyKey, "Property", null);

        final var morphismSig = new BaseSignature(500);
        final var morphism = new SerializedMorphism(
                morphismSig, entityKey, propertyKey, Min.ZERO, Set.of());
        final var morphismMetadata = new SerializedMetadataMorphism(morphismSig, "entityProperty");

        // Composite: CREATE Entity, CREATE Property, CREATE Entity->Property
        final var composite = new Composite("addProperty", List.of(
                new CreateObjex(entityObjex, entityMetadata),
                new CreateObjex(propertyObjex, propertyMetadata),
                new CreateMorphism(morphism, morphismMetadata)));

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .operation(composite)
                .expectedInstance(builder -> {
                    // Order remains, new entity with property structure created
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .run();
    }

    @Test
    void compositeDeleteMorphismThenObjex() {
        final var schema = Schema.newSchema();

        // First create structure, then delete it via composite
        final var entityKey = new Key(1000);
        final var propertyKey = new Key(1001);

        final var entityObjex = new SerializedObjex(entityKey, ObjexIds.fromIds(SignatureId.empty()));
        final var propertyObjex = new SerializedObjex(propertyKey, ObjexIds.fromIds(SignatureId.empty()));

        final var entityMetadata = new SerializedMetadataObjex(entityKey, "Entity", null);
        final var propertyMetadata = new SerializedMetadataObjex(propertyKey, "Property", null);

        final var morphismSig = new BaseSignature(500);
        final var morphism = new SerializedMorphism(
                morphismSig, entityKey, propertyKey, Min.ZERO, Set.of());
        final var morphismMetadata = new SerializedMetadataMorphism(morphismSig, "entityProperty");

        // Create structure
        final var createComposite = new Composite("createStructure", List.of(
                new CreateObjex(entityObjex, entityMetadata),
                new CreateObjex(propertyObjex, propertyMetadata),
                new CreateMorphism(morphism, morphismMetadata)));

        // Delete structure (morphism first, then objexes)
        final var deleteComposite = new Composite("deleteStructure", List.of(
                new DeleteMorphism(morphism, morphismMetadata),
                new DeleteObjex(propertyObjex, propertyMetadata),
                new DeleteObjex(entityObjex, entityMetadata)));

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .operation(createComposite)
                .operation(deleteComposite)
                .expectedInstance(builder -> {
                    // Back to just the order
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .run();
    }

    @Test
    void nestedComposite() {
        final var schema = Schema.newSchema();

        // Nested composites
        final var objex1Key = new Key(1000);
        final var objex2Key = new Key(1001);

        final var objex1 = new SerializedObjex(objex1Key, ObjexIds.fromIds(SignatureId.empty()));
        final var objex2 = new SerializedObjex(objex2Key, ObjexIds.fromIds(SignatureId.empty()));

        final var metadata1 = new SerializedMetadataObjex(objex1Key, "Entity1", null);
        final var metadata2 = new SerializedMetadataObjex(objex2Key, "Entity2", null);

        final var innerComposite = new Composite("inner", List.of(
                new CreateObjex(objex1, metadata1)));

        final var outerComposite = new Composite("outer", List.of(
                innerComposite,
                new CreateObjex(objex2, metadata2)));

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    // Empty instance
                })
                .operation(outerComposite)
                .expectedInstance(builder -> {
                    // Both objexes created
                })
                .run();
    }

    @Test
    void emptyComposite() {
        final var schema = Schema.newSchema();

        // Empty composite should be a no-op
        final var composite = new Composite("empty", List.of());

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    PostgreSQL.addOrder(builder, "o_100");
                    PostgreSQL.addProduct(builder, "p_1", "Widget", "5.00");
                })
                .operation(composite)
                .expectedInstance(builder -> {
                    // Unchanged
                    PostgreSQL.addOrder(builder, "o_100");
                    PostgreSQL.addProduct(builder, "p_1", "Widget", "5.00");
                })
                .run();
    }
}
