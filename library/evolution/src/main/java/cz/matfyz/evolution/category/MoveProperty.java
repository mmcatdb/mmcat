package cz.matfyz.evolution.category;

import java.util.List;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataMorphism;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;

/**
 * MOVE operation: Copy property to new location and delete the original.
 *
 * This operation moves a property (morphism) from one location to another.
 * It combines the COPY operation with deletion of the original.
 *
 * Example transformation:
 * Before: Customer.firstName, Customer.lastName, Customer.name (empty)
 * After: Customer.name.firstName, Customer.name.lastName (original morphisms deleted)
 *
 * Schema changes:
 * 1. Create new morphism(s) at target location
 * 2. Delete original morphism(s) from source location
 *
 * Instance changes:
 * - Copy property values/mappings to new morphisms
 * - Delete original mappings
 */
public record MoveProperty(
    /** The source object key (where properties are being moved FROM). */
    Key sourceKey,
    /** The target object key (where properties are being moved TO). */
    Key targetKey,
    /** The properties being moved. */
    List<MovedMorphism> movedMorphisms
) implements SMO {

    /**
     * Represents a morphism being moved from source to target.
     */
    public record MovedMorphism(
        /** The original morphism (to be deleted). */
        SerializedMorphism sourceMorphism,
        SerializedMetadataMorphism sourceMetadata,
        /** The new morphism at target location. */
        SerializedMorphism targetMorphism,
        SerializedMetadataMorphism targetMetadata
    ) {}

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    /**
     * Apply MOVE: create new morphisms at target, delete original morphisms.
     */
    @Override public void up(SchemaCategory schema, MetadataCategory metadata) {
        // 1. Create new morphisms at target location
        for (final var moved : movedMorphisms()) {
            schema.addMorphism(moved.targetMorphism());
        }

        // 2. Delete original morphisms from source
        for (final var moved : movedMorphisms()) {
            schema.removeMorphism(moved.sourceMorphism());
        }
    }

    /**
     * Revert MOVE: recreate original morphisms, delete target morphisms.
     */
    @Override public void down(SchemaCategory schema, MetadataCategory metadata) {
        // Reverse operations in reverse order

        // 2. Recreate original morphisms
        for (final var moved : movedMorphisms().reversed()) {
            schema.addMorphism(moved.sourceMorphism());
        }

        // 1. Delete target morphisms
        for (final var moved : movedMorphisms().reversed()) {
            schema.removeMorphism(moved.targetMorphism());
        }
    }

    /**
     * Helper to get the signature of a source morphism being moved.
     */
    public BaseSignature getSourceSignature(int index) {
        return movedMorphisms.get(index).sourceMorphism().signature();
    }

    /**
     * Helper to get the signature of a target morphism.
     */
    public BaseSignature getTargetSignature(int index) {
        return movedMorphisms.get(index).targetMorphism().signature();
    }

}
