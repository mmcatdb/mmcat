package cz.matfyz.evolution.category;

import java.util.List;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataMorphism;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObjex;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;

/**
 * JOIN operation: Combine multiple properties into one (or fewer) properties.
 *
 * Example transformation:
 * Before: Customer.firstName, Customer.lastName
 * After: Customer.name (where name = firstName + " " + lastName)
 *
 * The transformation function Φ is defined by a delimiter for concatenation.
 *
 * Schema changes:
 * 1. Create target property object(s) and morphism(s)
 * 2. Optionally delete source property objects and morphisms
 *
 * Instance changes:
 * - For each parent row, concatenate source property values with delimiter
 * - Store result in target property
 * - Optionally remove source property values
 */
public record JoinProperties(
    /** The parent object key (e.g., Customer). */
    Key parentKey,
    /** The source properties to join (ordered). */
    List<SourceProperty> sourceProperties,
    /** The target property to create. */
    TargetProperty targetProperty,
    /** The delimiter to use when joining values (e.g., " " or ", "). */
    String delimiter,
    /** Whether to delete the source properties after joining. */
    boolean deleteSourceProperties
) implements SMO {

    /**
     * Represents a source property to be joined.
     */
    public record SourceProperty(
        SerializedObjex objex,
        SerializedMetadataObjex objexMetadata,
        SerializedMorphism morphism,
        SerializedMetadataMorphism morphismMetadata
    ) {}

    /**
     * Represents the target property created by the join.
     */
    public record TargetProperty(
        SerializedObjex objex,
        SerializedMetadataObjex objexMetadata,
        SerializedMorphism morphism,
        SerializedMetadataMorphism morphismMetadata
    ) {}

    @Override public <T> T accept(SchemaEvolutionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    /**
     * Apply JOIN: create target property, optionally delete source properties.
     */
    @Override public void up(SchemaCategory schema, MetadataCategory metadata) {
        // 1. Create target property object and morphism
        schema.addObjex(targetProperty.objex());
        schema.addMorphism(targetProperty.morphism());

        // 2. Optionally delete source properties
        if (deleteSourceProperties) {
            for (final var source : sourceProperties) {
                schema.removeMorphism(source.morphism());
                schema.removeObjex(source.objex());
            }
        }
    }

    /**
     * Revert JOIN: delete target property, optionally recreate source properties.
     */
    @Override public void down(SchemaCategory schema, MetadataCategory metadata) {
        // Reverse operations

        // 2. Recreate source properties if they were deleted
        if (deleteSourceProperties) {
            for (final var source : sourceProperties.reversed()) {
                schema.addObjex(source.objex());
                schema.addMorphism(source.morphism());
            }
        }

        // 1. Delete target property
        schema.removeMorphism(targetProperty.morphism());
        schema.removeObjex(targetProperty.objex());
    }

}
