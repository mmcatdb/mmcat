package cz.matfyz.evolution.category;

import java.util.List;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataMorphism;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObjex;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;

/**
 * UNGROUP operation: Inverse of GROUP.
 *
 * Moves properties from a grouping object back to the parent object.
 *
 * Example transformation:
 * Before: Customer.name.firstName, Customer.name.lastName
 * After: Customer.firstName, Customer.lastName
 *
 * Schema changes:
 * 1. Create new morphisms from parent to each property
 * 2. Delete old morphisms (from grouping to properties)
 * 3. Delete morphism from parent to grouping
 * 4. Delete the grouping object
 *
 * Instance changes:
 * - For each grouping row, migrate property values/mappings back to parent row
 * - Delete grouping rows and parent-to-grouping mappings
 */
public record UngroupProperty(
    /** The parent object key (e.g., Customer). */
    Key parentKey,
    /** The grouping object to be removed (serialized for reconstruction in down()). */
    SerializedObjex groupingObjex,
    SerializedMetadataObjex groupingObjexMetadata,
    /** The morphism from parent to grouping object (to be deleted). */
    SerializedMorphism parentToGroupMorphism,
    SerializedMetadataMorphism parentToGroupMorphismMetadata,
    /** The morphisms being moved from grouping object back to parent. */
    List<MovedMorphism> movedMorphisms
) implements SMO {

    /**
     * Represents a morphism being moved from grouping object to parent.
     */
    public record MovedMorphism(
        /** The original morphism from grouping to property (to be deleted). */
        SerializedMorphism oldMorphism,
        SerializedMetadataMorphism oldMetadata,
        /** The new morphism from parent to property. */
        SerializedMorphism newMorphism,
        SerializedMetadataMorphism newMetadata
    ) {}

    public Key groupingObjexKey() {
        return groupingObjex.key();
    }

    public BaseSignature parentToGroupSignature() {
        return parentToGroupMorphism.signature();
    }

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    /**
     * Apply UNGROUP: move properties from grouping object back to parent and delete grouping.
     */
    @Override public void up(SchemaCategory schema, MetadataCategory metadata) {
        // 1. Create new morphisms from parent to each property
        for (final var moved : movedMorphisms()) {
            schema.addMorphism(moved.newMorphism());
        }

        // 2. Delete old morphisms (from grouping to properties)
        for (final var moved : movedMorphisms()) {
            schema.removeMorphism(moved.oldMorphism());
        }

        // 3. Delete morphism from parent to grouping
        schema.removeMorphism(parentToGroupMorphism);

        // 4. Delete the grouping object
        schema.removeObjex(groupingObjex);
    }

    /**
     * Revert UNGROUP: recreate grouping object and move properties back to it.
     * This is essentially the GROUP operation.
     */
    @Override public void down(SchemaCategory schema, MetadataCategory metadata) {
        // Reverse operations in reverse order

        // 4. Recreate the grouping object
        schema.addObjex(groupingObjex);

        // 3. Recreate morphism from parent to grouping
        schema.addMorphism(parentToGroupMorphism);

        // 2. Recreate old morphisms (grouping to properties)
        for (final var moved : movedMorphisms().reversed()) {
            schema.addMorphism(moved.oldMorphism());
        }

        // 1. Delete new morphisms (parent to properties)
        for (final var moved : movedMorphisms().reversed()) {
            schema.removeMorphism(moved.newMorphism());
        }
    }

}
