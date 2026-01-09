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
import cz.matfyz.evolution.category.CopyObjex;
import cz.matfyz.evolution.category.CreateMorphism;
import cz.matfyz.evolution.category.CreateObjex;
import cz.matfyz.tests.example.basic.PostgreSQL;
import cz.matfyz.tests.example.basic.Schema;

/**
 * Tests for the CopyObjex evolution operation.
 * 
 * NOTE: Standalone objexes without morphisms are NON-ENTITIES.
 * InstanceBuilder only creates InstanceObjex for ENTITIES (objexes with outgoing morphisms).
 * So tests with standalone objexes won't have visible InstanceObjex in the instance.
 * 
 * For meaningful tests, we need to create objexes WITH morphisms to make them entities.
 */
class CopyObjexTests {

    @BeforeEach
    void setup() {
        UniqueIdGenerator.makeDeterministic();
    }

    /**
     * Test CopyObjex with an entity (has morphism, so creates InstanceObjex).
     * Creates: Entity1 --(morphism)--> Property1
     * Copies to: Entity2 --(morphism)--> Property1 (redirected)
     */
    @Test
    void copyEntityObjex() {
        final var schema = Schema.newSchema();

        // Source: Entity1 (1000) -> Property1 (1001)
        final var entity1Key = new Key(1000);
        final var property1Key = new Key(1001);
        
        final var entity1 = new SerializedObjex(entity1Key, ObjexIds.fromIds(SignatureId.empty()));
        final var property1 = new SerializedObjex(property1Key, ObjexIds.fromIds(SignatureId.empty()));
        
        final var entity1Meta = new SerializedMetadataObjex(entity1Key, "Entity1", null);
        final var property1Meta = new SerializedMetadataObjex(property1Key, "Property1", null);
        
        final var createEntity1 = new CreateObjex(entity1, entity1Meta);
        final var createProperty1 = new CreateObjex(property1, property1Meta);
        
        // Morphism makes Entity1 an entity (has outgoing morphism)
        final var morphism1Sig = new BaseSignature(500);
        final var morphism1 = new SerializedMorphism(morphism1Sig, entity1Key, property1Key, Min.ZERO, Set.of());
        final var morphism1Meta = new SerializedMetadataMorphism(morphism1Sig, "entity1ToProperty1");
        final var createMorphism1 = new CreateMorphism(morphism1, morphism1Meta);

        // Target: Entity2 (1002) -> Property1 (1001) via redirected morphism
        final var entity2Key = new Key(1002);
        
        final var entity2 = new SerializedObjex(entity2Key, ObjexIds.fromIds(SignatureId.empty()));
        
        final var entity2Meta = new SerializedMetadataObjex(entity2Key, "Entity2", null);
        
        final var createEntity2 = new CreateObjex(entity2, entity2Meta);
        
        final var morphism2Sig = new BaseSignature(501);
        final var morphism2 = new SerializedMorphism(morphism2Sig, entity2Key, property1Key, Min.ZERO, Set.of()); // points to same property1
        final var morphism2Meta = new SerializedMetadataMorphism(morphism2Sig, "entity2ToProperty1");
        final var createMorphism2 = new CreateMorphism(morphism2, morphism2Meta);

        // CopyObjex copies from entity1 to entity2, redirecting morphisms
        final var copyObjex = new CopyObjex(
                entity1,
                entity2,
                List.of(createEntity2, createMorphism2));

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    PostgreSQL.addOrder(builder, "o_100");
                })
                .operation(createEntity1)
                .operation(createProperty1)
                .operation(createMorphism1)
                .operation(copyObjex)
                .expectedInstance(builder -> {
                    // Order remains
                    PostgreSQL.addOrder(builder, "o_100");
                    // Entity1 and Entity2 are both entities (have outgoing morphisms)
                    // They exist in the instance but are empty (no data added)
                })
                .run();
    }

    @Test
    void copyObjexPreservesExistingData() {
        final var schema = Schema.newSchema();

        // Create entity structure
        final var entity1Key = new Key(1000);
        final var property1Key = new Key(1001);
        
        final var entity1 = new SerializedObjex(entity1Key, ObjexIds.fromIds(SignatureId.empty()));
        final var property1 = new SerializedObjex(property1Key, ObjexIds.fromIds(SignatureId.empty()));
        
        final var entity1Meta = new SerializedMetadataObjex(entity1Key, "Entity1", null);
        final var property1Meta = new SerializedMetadataObjex(property1Key, "Property1", null);
        
        final var createEntity1 = new CreateObjex(entity1, entity1Meta);
        final var createProperty1 = new CreateObjex(property1, property1Meta);
        
        final var morphism1Sig = new BaseSignature(500);
        final var morphism1 = new SerializedMorphism(morphism1Sig, entity1Key, property1Key, Min.ZERO, Set.of());
        final var morphism1Meta = new SerializedMetadataMorphism(morphism1Sig, "entity1Prop");
        final var createMorphism1 = new CreateMorphism(morphism1, morphism1Meta);

        new EvolutionTestBase()
                .schema(schema)
                .initialInstance(builder -> {
                    PostgreSQL.addOrder(builder, "o_100");
                    PostgreSQL.addOrder(builder, "o_200");
                    PostgreSQL.addProduct(builder, "p_1", "Widget", "9.99");
                })
                .operation(createEntity1)
                .operation(createProperty1)
                .operation(createMorphism1)
                .expectedInstance(builder -> {
                    // All existing data preserved
                    PostgreSQL.addOrder(builder, "o_100");
                    PostgreSQL.addOrder(builder, "o_200");
                    PostgreSQL.addProduct(builder, "p_1", "Widget", "9.99");
                    // New entity exists but is empty
                })
                .run();
    }
}
