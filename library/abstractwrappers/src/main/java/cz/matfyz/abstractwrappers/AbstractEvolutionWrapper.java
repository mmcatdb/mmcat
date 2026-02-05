package cz.matfyz.abstractwrappers;

import java.util.List;

/**
 * Interface for generating database-specific migration statements
 * when schema evolution operations (SMOs) are propagated to actual databases.
 *
 * Each database type (PostgreSQL, MongoDB, etc.) implements this interface
 * to translate categorical SMOs into concrete database commands.
 *
 * Two contrasting strategies emerge:
 * - Schema-aware databases (PostgreSQL): ALTER TABLE, UPDATE with SQL functions
 * - Schemaless databases (MongoDB): updateMany with aggregation pipeline ($set, $unset, $concat, $split)
 */
public interface AbstractEvolutionWrapper {

    /**
     * Generate statements for creating a new database kind (table/collection).
     * @param kindName The table or collection name.
     * @param properties Properties to include in the initial schema (for schema-aware DBs).
     */
    List<AbstractStatement> evolveCreateObjex(String kindName, List<PropertyInfo> properties);

    /**
     * Generate statements for dropping a database kind.
     * @param kindName The table or collection name to drop.
     */
    List<AbstractStatement> evolveDeleteObjex(String kindName);

    /**
     * Generate statements for adding a column/field to an existing kind.
     * @param kindName The table or collection name.
     * @param propertyName The new column/field name.
     * @param isRequired Whether the property is NOT NULL / required.
     */
    List<AbstractStatement> evolveAddProperty(String kindName, String propertyName, boolean isRequired);

    /**
     * Generate statements for removing a column/field from an existing kind.
     * @param kindName The table or collection name.
     * @param propertyName The column/field to remove.
     */
    List<AbstractStatement> evolveRemoveProperty(String kindName, String propertyName);

    /**
     * Generate statements for GROUP: nest multiple properties under a new sub-structure.
     *
     * PostgreSQL: Uses JSONB column to store grouped properties.
     * MongoDB: Uses native document nesting via $set.
     *
     * @param kindName The table or collection containing the properties.
     * @param groupName The name of the new grouping structure.
     * @param propertyNames The properties to group.
     */
    List<AbstractStatement> evolveGroupProperties(String kindName, String groupName, List<String> propertyNames);

    /**
     * Generate statements for UNGROUP: flatten a nested structure back to top-level properties.
     *
     * @param kindName The table or collection.
     * @param groupName The grouping structure to flatten.
     * @param propertyNames The properties inside the group.
     */
    List<AbstractStatement> evolveUngroupProperty(String kindName, String groupName, List<String> propertyNames);

    /**
     * Generate statements for JOIN: concatenate multiple properties into one using a delimiter.
     *
     * PostgreSQL: UPDATE SET target = col1 || delimiter || col2
     * MongoDB: updateMany with $concat
     *
     * @param kindName The table or collection.
     * @param targetProperty The new concatenated property name.
     * @param sourceProperties The source properties to join (in order).
     * @param delimiter The delimiter string between values.
     * @param deleteSources Whether to remove the source properties after joining.
     */
    List<AbstractStatement> evolveJoinProperties(
        String kindName, String targetProperty,
        List<String> sourceProperties, String delimiter,
        boolean deleteSources
    );

    /**
     * Generate statements for SPLIT: split one property into multiple by delimiter.
     *
     * PostgreSQL: UPDATE SET c1 = SPLIT_PART(source, delimiter, 1), ...
     * MongoDB: updateMany with $split and $arrayElemAt
     *
     * @param kindName The table or collection.
     * @param sourceProperty The property to split.
     * @param targetProperties The target property names (in order).
     * @param delimiter The delimiter to split by.
     * @param deleteSource Whether to remove the source property after splitting.
     */
    List<AbstractStatement> evolveSplitProperty(
        String kindName, String sourceProperty,
        List<String> targetProperties, String delimiter,
        boolean deleteSource
    );

    /**
     * Generate statements for MOVE: copy a property value to a new location and delete the original.
     *
     * @param sourceKindName The source table or collection.
     * @param sourceProperty The property to move.
     * @param targetKindName The target table or collection.
     * @param targetProperty The new property name at the target.
     */
    List<AbstractStatement> evolveMoveProperty(
        String sourceKindName, String sourceProperty,
        String targetKindName, String targetProperty
    );

    /**
     * Property metadata used when creating new database kinds.
     */
    record PropertyInfo(String name, boolean isRequired, boolean isComplex) {}

}
