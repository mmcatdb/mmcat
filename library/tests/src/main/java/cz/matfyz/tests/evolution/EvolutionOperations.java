package cz.matfyz.tests.evolution;

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
import cz.matfyz.evolution.category.CreateMorphism;
import cz.matfyz.evolution.category.CreateObjex;
import cz.matfyz.evolution.category.DeleteMorphism;
import cz.matfyz.evolution.category.DeleteObjex;
import cz.matfyz.evolution.category.UpdateMorphism;
import cz.matfyz.evolution.category.UpdateObjex;

/**
 * Helper class for creating evolution operations in tests.
 * 
 * Provides factory methods to create SMO operations with less boilerplate.
 * Uses a fluent builder pattern for common scenarios.
 */
public class EvolutionOperations {

    private EvolutionOperations() {}

    // === Objex Operations ===

    /**
     * Creates a CreateObjex operation for a new entity.
     * 
     * @param key The key for the new objex
     * @param label The human-readable label
     * @return CreateObjex operation
     */
    public static CreateObjex createObjex(int key, String label) {
        final var objexKey = new Key(key);
        final var serializedObjex = new SerializedObjex(
            objexKey,
            ObjexIds.fromIds(SignatureId.empty())
        );
        final var metadata = new SerializedMetadataObjex(objexKey, label, null);
        return new CreateObjex(serializedObjex, metadata);
    }

    /**
     * Creates a DeleteObjex operation.
     */
    public static DeleteObjex deleteObjex(int key, String label) {
        final var objexKey = new Key(key);
        final var serializedObjex = new SerializedObjex(
            objexKey,
            ObjexIds.fromIds(SignatureId.empty())
        );
        final var metadata = new SerializedMetadataObjex(objexKey, label, null);
        return new DeleteObjex(serializedObjex, metadata);
    }

    /**
     * Creates an UpdateObjex operation.
     */
    public static UpdateObjex updateObjex(int key, ObjexIds oldIds, ObjexIds newIds) {
        final var objexKey = new Key(key);
        final var oldObjex = new SerializedObjex(objexKey, oldIds);
        final var newObjex = new SerializedObjex(objexKey, newIds);
        return new UpdateObjex(newObjex, oldObjex);
    }

    // === Morphism Operations ===

    /**
     * Creates a CreateMorphism operation with default cardinality (0..1).
     */
    public static CreateMorphism createMorphism(int signature, int domKey, int codKey, String label) {
        return createMorphism(signature, domKey, codKey, label, Min.ZERO);
    }

    /**
     * Creates a CreateMorphism operation with specified cardinality.
     */
    public static CreateMorphism createMorphism(int signature, int domKey, int codKey, String label, Min min) {
        final var sig = new BaseSignature(signature);
        final var serializedMorphism = new SerializedMorphism(
            sig,
            new Key(domKey),
            new Key(codKey),
            min,
            Set.of()
        );
        final var metadata = new SerializedMetadataMorphism(sig, label);
        return new CreateMorphism(serializedMorphism, metadata);
    }

    /**
     * Creates a DeleteMorphism operation.
     */
    public static DeleteMorphism deleteMorphism(int signature, int domKey, int codKey, String label) {
        final var sig = new BaseSignature(signature);
        final var serializedMorphism = new SerializedMorphism(
            sig,
            new Key(domKey),
            new Key(codKey),
            Min.ZERO,
            Set.of()
        );
        final var metadata = new SerializedMetadataMorphism(sig, label);
        return new DeleteMorphism(serializedMorphism, metadata);
    }

    /**
     * Creates an UpdateMorphism operation to change cardinality.
     */
    public static UpdateMorphism updateMorphismCardinality(int signature, int domKey, int codKey, Min oldMin, Min newMin) {
        final var sig = new BaseSignature(signature);
        final var oldMorphism = new SerializedMorphism(sig, new Key(domKey), new Key(codKey), oldMin, Set.of());
        final var newMorphism = new SerializedMorphism(sig, new Key(domKey), new Key(codKey), newMin, Set.of());
        return new UpdateMorphism(newMorphism, oldMorphism);
    }

    // === Composite Scenarios ===

    /**
     * Builder for creating an "ADD Property" composite operation.
     * This creates:
     * 1. A new objex for the property value
     * 2. A morphism from the parent to the property
     */
    public static class AddPropertyBuilder {
        private final int parentKey;
        private int propertyKey;
        private int morphismSig;
        private String propertyLabel;
        private Min cardinality = Min.ZERO;

        public AddPropertyBuilder(int parentKey) {
            this.parentKey = parentKey;
        }

        public AddPropertyBuilder propertyKey(int key) {
            this.propertyKey = key;
            return this;
        }

        public AddPropertyBuilder morphismSignature(int sig) {
            this.morphismSig = sig;
            return this;
        }

        public AddPropertyBuilder label(String label) {
            this.propertyLabel = label;
            return this;
        }

        public AddPropertyBuilder required() {
            this.cardinality = Min.ONE;
            return this;
        }

        public CreateObjex buildCreateObjex() {
            return createObjex(propertyKey, propertyLabel);
        }

        public CreateMorphism buildCreateMorphism() {
            return createMorphism(morphismSig, parentKey, propertyKey, propertyLabel, cardinality);
        }
    }

    public static AddPropertyBuilder addProperty(int parentKey) {
        return new AddPropertyBuilder(parentKey);
    }
}
