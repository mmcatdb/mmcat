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
 * SPLIT operation: Split one property into multiple properties.
 *
 * Example transformation:
 * Before: Customer.address ("street,number,postcode")
 * After: Customer.street, Customer.number, Customer.postcode
 *
 * The transformation function Φ is defined by a delimiter for splitting.
 *
 * Schema changes:
 * 1. Create target property objects and morphisms
 * 2. Optionally delete source property object and morphism
 *
 * Instance changes:
 * - For each parent row, split the source property value by delimiter
 * - Store each part in corresponding target property
 * - Optionally remove source property value
 */
public record SplitProperty(
    /** The parent object key (e.g., Customer). */
    Key parentKey,
    /** The source property to split. */
    SourceProperty sourceProperty,
    /** The target properties to create (ordered - index determines which split part). */
    List<TargetProperty> targetProperties,
    /** The delimiter to use when splitting values (e.g., "," or " "). */
    String delimiter,
    /** Whether to delete the source property after splitting. */
    boolean deleteSourceProperty
) implements SMO {

    /**
     * Represents the source property to be split.
     */
    public record SourceProperty(
        SerializedObjex objex,
        SerializedMetadataObjex objexMetadata,
        SerializedMorphism morphism,
        SerializedMetadataMorphism morphismMetadata
    ) {}

    /**
     * Represents a target property created by the split.
     * The index in the list determines which part of the split value it receives.
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
     * Apply SPLIT: create target properties, optionally delete source property.
     */
    @Override public void up(SchemaCategory schema, MetadataCategory metadata) {
        // 1. Create target property objects and morphisms
        for (final var target : targetProperties) {
            schema.addObjex(target.objex());
            schema.addMorphism(target.morphism());
        }

        // 2. Optionally delete source property
        if (deleteSourceProperty) {
            schema.removeMorphism(sourceProperty.morphism());
            schema.removeObjex(sourceProperty.objex());
        }
    }

    /**
     * Revert SPLIT: delete target properties, optionally recreate source property.
     */
    @Override public void down(SchemaCategory schema, MetadataCategory metadata) {
        // Reverse operations

        // 2. Recreate source property if it was deleted
        if (deleteSourceProperty) {
            schema.addObjex(sourceProperty.objex());
            schema.addMorphism(sourceProperty.morphism());
        }

        // 1. Delete target properties
        for (final var target : targetProperties.reversed()) {
            schema.removeMorphism(target.morphism());
            schema.removeObjex(target.objex());
        }
    }

}
