package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractEvolutionWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.AbstractStatement.StringStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * PostgreSQL implementation of evolution wrapper.
 * Generates ALTER TABLE, UPDATE, and other DDL/DML statements
 * to propagate schema evolution operations to PostgreSQL databases.
 *
 * Key design decisions for PostgreSQL:
 * - GROUP uses JSONB columns to represent nested structures (contrasts with MongoDB's native nesting)
 * - JOIN uses string concatenation (||) operator
 * - SPLIT uses SPLIT_PART() function
 * - All identifiers are double-quoted for safety
 */
public class PostgreSQLEvolutionWrapper implements AbstractEvolutionWrapper {

    @Override
    public List<AbstractStatement> evolveCreateObjex(String kindName, List<PropertyInfo> properties) {
        final var statements = new ArrayList<AbstractStatement>();

        final var columns = new StringBuilder();
        columns.append("\"_id\" SERIAL PRIMARY KEY");
        for (final var prop : properties) {
            columns.append(",\n    \"").append(escape(prop.name())).append("\" TEXT");
            if (prop.isRequired()) {
                columns.append(" NOT NULL");
            }
        }

        statements.add(StringStatement.create(
            "CREATE TABLE IF NOT EXISTS \"" + escape(kindName) + "\" (\n    " + columns + "\n);"
        ));

        return statements;
    }

    @Override
    public List<AbstractStatement> evolveDeleteObjex(String kindName) {
        return List.of(
            StringStatement.create("DROP TABLE IF EXISTS \"" + escape(kindName) + "\" CASCADE;")
        );
    }

    @Override
    public List<AbstractStatement> evolveAddProperty(String kindName, String propertyName, boolean isRequired) {
        final String nullConstraint = isRequired ? " NOT NULL DEFAULT ''" : "";
        return List.of(
            StringStatement.create(
                "ALTER TABLE \"" + escape(kindName) + "\" ADD COLUMN \"" + escape(propertyName) + "\" TEXT" + nullConstraint + ";"
            )
        );
    }

    @Override
    public List<AbstractStatement> evolveRemoveProperty(String kindName, String propertyName) {
        return List.of(
            StringStatement.create(
                "ALTER TABLE \"" + escape(kindName) + "\" DROP COLUMN IF EXISTS \"" + escape(propertyName) + "\";"
            )
        );
    }

    @Override
    public List<AbstractStatement> evolveGroupProperties(String kindName, String groupName, List<String> propertyNames) {
        final var statements = new ArrayList<AbstractStatement>();

        // Step 1: Add JSONB column for the group
        statements.add(StringStatement.create(
            "ALTER TABLE \"" + escape(kindName) + "\" ADD COLUMN \"" + escape(groupName) + "\" JSONB;"
        ));

        // Step 2: Populate the JSONB column from existing columns
        final String jsonBuild = propertyNames.stream()
            .map(p -> "'" + escape(p) + "', \"" + escape(p) + "\"")
            .collect(Collectors.joining(", "));
        statements.add(StringStatement.create(
            "UPDATE \"" + escape(kindName) + "\" SET \"" + escape(groupName) + "\" = jsonb_build_object(" + jsonBuild + ");"
        ));

        // Step 3: Drop the original columns
        for (final String prop : propertyNames) {
            statements.add(StringStatement.create(
                "ALTER TABLE \"" + escape(kindName) + "\" DROP COLUMN IF EXISTS \"" + escape(prop) + "\";"
            ));
        }

        return statements;
    }

    @Override
    public List<AbstractStatement> evolveUngroupProperty(String kindName, String groupName, List<String> propertyNames) {
        final var statements = new ArrayList<AbstractStatement>();

        // Step 1: Add columns for each property being ungrouped
        for (final String prop : propertyNames) {
            statements.add(StringStatement.create(
                "ALTER TABLE \"" + escape(kindName) + "\" ADD COLUMN \"" + escape(prop) + "\" TEXT;"
            ));
        }

        // Step 2: Populate from JSONB column
        final String setClauses = propertyNames.stream()
            .map(p -> "\"" + escape(p) + "\" = \"" + escape(groupName) + "\"->>" + "'" + escape(p) + "'")
            .collect(Collectors.joining(", "));
        statements.add(StringStatement.create(
            "UPDATE \"" + escape(kindName) + "\" SET " + setClauses + ";"
        ));

        // Step 3: Drop the JSONB group column
        statements.add(StringStatement.create(
            "ALTER TABLE \"" + escape(kindName) + "\" DROP COLUMN IF EXISTS \"" + escape(groupName) + "\";"
        ));

        return statements;
    }

    @Override
    public List<AbstractStatement> evolveJoinProperties(
        String kindName, String targetProperty,
        List<String> sourceProperties, String delimiter,
        boolean deleteSources
    ) {
        final var statements = new ArrayList<AbstractStatement>();

        // Step 1: Add target column
        statements.add(StringStatement.create(
            "ALTER TABLE \"" + escape(kindName) + "\" ADD COLUMN \"" + escape(targetProperty) + "\" TEXT;"
        ));

        // Step 2: Populate with concatenation
        final String concatExpr = sourceProperties.stream()
            .map(p -> "COALESCE(\"" + escape(p) + "\", '')")
            .collect(Collectors.joining(" || '" + escape(delimiter) + "' || "));
        statements.add(StringStatement.create(
            "UPDATE \"" + escape(kindName) + "\" SET \"" + escape(targetProperty) + "\" = " + concatExpr + ";"
        ));

        // Step 3: Optionally drop source columns
        if (deleteSources) {
            for (final String src : sourceProperties) {
                statements.add(StringStatement.create(
                    "ALTER TABLE \"" + escape(kindName) + "\" DROP COLUMN IF EXISTS \"" + escape(src) + "\";"
                ));
            }
        }

        return statements;
    }

    @Override
    public List<AbstractStatement> evolveSplitProperty(
        String kindName, String sourceProperty,
        List<String> targetProperties, String delimiter,
        boolean deleteSource
    ) {
        final var statements = new ArrayList<AbstractStatement>();

        // Step 1: Add target columns
        for (final String target : targetProperties) {
            statements.add(StringStatement.create(
                "ALTER TABLE \"" + escape(kindName) + "\" ADD COLUMN \"" + escape(target) + "\" TEXT;"
            ));
        }

        // Step 2: Populate using SPLIT_PART
        final String setClauses = IntStream.range(0, targetProperties.size())
            .mapToObj(i -> "\"" + escape(targetProperties.get(i)) + "\" = TRIM(SPLIT_PART(\"" + escape(sourceProperty) + "\", '" + escape(delimiter) + "', " + (i + 1) + "))")
            .collect(Collectors.joining(", "));
        statements.add(StringStatement.create(
            "UPDATE \"" + escape(kindName) + "\" SET " + setClauses + ";"
        ));

        // Step 3: Optionally drop source column
        if (deleteSource) {
            statements.add(StringStatement.create(
                "ALTER TABLE \"" + escape(kindName) + "\" DROP COLUMN IF EXISTS \"" + escape(sourceProperty) + "\";"
            ));
        }

        return statements;
    }

    @Override
    public List<AbstractStatement> evolveMoveProperty(
        String sourceKindName, String sourceProperty,
        String targetKindName, String targetProperty
    ) {
        final var statements = new ArrayList<AbstractStatement>();

        if (sourceKindName.equals(targetKindName)) {
            // Same table: just rename the column
            statements.add(StringStatement.create(
                "ALTER TABLE \"" + escape(sourceKindName) + "\" RENAME COLUMN \"" + escape(sourceProperty) + "\" TO \"" + escape(targetProperty) + "\";"
            ));
        } else {
            // Different tables: add column to target, copy data, drop from source
            statements.add(StringStatement.create(
                "ALTER TABLE \"" + escape(targetKindName) + "\" ADD COLUMN \"" + escape(targetProperty) + "\" TEXT;"
            ));
            // Note: cross-table data migration requires a join condition.
            // For now, generate a comment indicating manual intervention may be needed.
            statements.add(StringStatement.create(
                "UPDATE \"" + escape(targetKindName) + "\" t SET \"" + escape(targetProperty) + "\" = s.\"" + escape(sourceProperty) + "\" FROM \"" + escape(sourceKindName) + "\" s WHERE t.\"_id\" = s.\"_id\";"
            ));
            statements.add(StringStatement.create(
                "ALTER TABLE \"" + escape(sourceKindName) + "\" DROP COLUMN IF EXISTS \"" + escape(sourceProperty) + "\";"
            ));
        }

        return statements;
    }

    /**
     * Escape single quotes in SQL strings to prevent injection.
     */
    private static String escape(String value) {
        return value.replace("'", "''").replace("\"", "\"\"");
    }

}
