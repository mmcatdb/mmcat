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
 * GROUP operation: Groups multiple properties under a new intermediate object.
 *
 * Example:
 * Before: Customer.firstName, Customer.lastName
 * After: Customer.name.firstName, Customer.name.lastName
 *
 * Schema changes:
 * 1. Create the new grouping objex (e.g., "name")
 * 2. Create morphism from parent to grouping objex (e.g., Customer -> name)
 * 3. For each property being grouped:
 *    - Delete old morphism (parent -> property)
 *    - Create new morphism (groupingObjex -> property)
 *
 * Instance changes:
 * - For each row in parent objex, create corresponding row in grouping objex
 * - Migrate mappings from (parent -> property) to (groupingObjex -> property)
 */
public record GroupProperties(
    /** The key of the parent objex containing the properties to group. */
    Key parentKey,

    /** The new grouping objex to create. */
    SerializedObjex groupingObjex,
    SerializedMetadataObjex groupingObjexMetadata,

    /** The morphism from parent to the new grouping objex. */
    SerializedMorphism parentToGroupMorphism,
    SerializedMetadataMorphism parentToGroupMorphismMetadata,

    /**
     * The morphisms being moved under the grouping objex.
     * Each entry contains:
     * - oldMorphism: the morphism from parent to property (to be deleted)
     * - newMorphism: the morphism from groupingObjex to property (to be created)
     */
    List<MovedMorphism> movedMorphisms
) implements SMO {

    /**
     * Represents a morphism being moved from parent to the grouping objex.
     */
    public record MovedMorphism(
        SerializedMorphism oldMorphism,
        SerializedMetadataMorphism oldMetadata,
        SerializedMorphism newMorphism,
        SerializedMetadataMorphism newMetadata
    ) {}

    @Override
    public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void up(SchemaCategory schema, MetadataCategory metadata) {
        // 1. Create the new grouping objex
        schema.addObjex(groupingObjex);

        // 2. Create morphism from parent to grouping objex
        schema.addMorphism(parentToGroupMorphism);

        // 3. For each property being grouped:
        for (MovedMorphism moved : movedMorphisms) {
            // Delete old morphism (parent -> property)
            schema.removeMorphism(moved.oldMorphism());
            // Create new morphism (groupingObjex -> property)
            schema.addMorphism(moved.newMorphism());
        }
    }

    @Override
    public void down(SchemaCategory schema, MetadataCategory metadata) {
        // Reverse the operations in reverse order

        // 3. For each property (reverse order):
        for (MovedMorphism moved : movedMorphisms.reversed()) {
            // Delete new morphism
            schema.removeMorphism(moved.newMorphism());
            // Restore old morphism
            schema.addMorphism(moved.oldMorphism());
        }

        // 2. Delete morphism from parent to grouping objex
        schema.removeMorphism(parentToGroupMorphism);

        // 1. Delete the grouping objex
        schema.removeObjex(groupingObjex);
    }

    /**
     * Get the signature of the morphism from parent to grouping objex.
     */
    public BaseSignature parentToGroupSignature() {
        return parentToGroupMorphism.signature();
    }

    /**
     * Get the key of the grouping objex.
     */
    public Key groupingObjexKey() {
        return groupingObjex.key();
    }
}
