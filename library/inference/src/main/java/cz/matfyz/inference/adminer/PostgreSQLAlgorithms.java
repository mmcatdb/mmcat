package cz.matfyz.inference.adminer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.adminer.ReferenceKind;

public final class PostgreSQLAlgorithms implements AdminerAlgorithmsInterface {
    private static final PostgreSQLAlgorithms INSTANCE = new PostgreSQLAlgorithms();

    private PostgreSQLAlgorithms() {}

    /**
     * Retrieves the singleton instance of {@link PostgreSQLAlgorithms}.
     *
     * @return The single instance of {@link PostgreSQLAlgorithms}.
     */
    public static PostgreSQLAlgorithms getInstance() {
        return INSTANCE;
    }

    /**
     * Retrieves all property names for a given kind.
     *
     * @param stmt     The {@link Statement} object used to execute the query.
     * @param kindName The name of the kind whose property names are being retrieved.
     * @return A {@link Set} of property names for the specified kind.
     * @throws PullForestException if an error occurs during database access.
     */
    public static Set<String> getPropertyNames(Statement stmt, String kindName) {
        try {
            Set<String> properties = new HashSet<>();

            String query = String.format("""
                SELECT
                    column_name
                FROM
                    information_schema.columns
                WHERE
                    table_schema = 'public'
                    AND table_name = '%s'
                ORDER BY
                    ordinal_position;

                """, kindName);
            ResultSet resultSet = stmt.executeQuery(query);

            while (resultSet.next()) {
                String column = resultSet.getString(1);
                properties.add(column);
            }

            return properties;
        }
        catch (SQLException e) {
			throw PullForestException.innerException(e);
		}
    }

    /**
     * Retrieves foreign key relationships from the database for the specified kind.
     *
     * @param stmt          The {@link Statement} object used to execute the SQL query.
     * @param references    The list of references to which the foreign keys will be added.
     * @param datasourceId  The identifier of the data source.
     * @param kindName      The name of the kind for which foreign keys are retrieved.
     * @param outgoing      A boolean flag indicating the direction of the foreign key relationship:
     *                   <ul>
     *                       <li><code>true</code> for outgoing foreign keys (keys where the kind references other kinds).</li>
     *                       <li><code>false</code> for incoming foreign keys (keys where other kinds reference the kind).</li>
     *                   </ul>
     * @return A {@link List} of {@link Reference} objects representing the foreign key relationships for the specified kind.
     * @throws SQLException if an error occurs during database access.
     */
    private static List<Reference> getReferences(Statement stmt, List<Reference> references, String datasourceId, String kindName, boolean outgoing) throws SQLException {
        String referencedKind = outgoing ? "kcu" : "ccu";
        String referencingKind = outgoing ? "ccu" : "kcu";

        String query = String.format("""
            SELECT
                %s.column_name AS to_property,
                %s.table_name AS from_kind,
                %s.column_name AS from_property
            FROM
                information_schema.key_column_usage kcu
            JOIN
                information_schema.referential_constraints rc
                ON kcu.constraint_name = rc.constraint_name
                AND kcu.table_schema = rc.constraint_schema
            JOIN
                information_schema.constraint_column_usage ccu
                ON rc.unique_constraint_name = ccu.constraint_name
                AND rc.unique_constraint_schema = ccu.constraint_schema
            WHERE
                kcu.table_schema = 'public'
                AND %s.table_name = '%s';
            """, referencedKind, referencingKind, referencingKind, referencedKind, kindName);
        ResultSet result = stmt.executeQuery(query);

        while (result.next()) {
            String toProperty = result.getString("to_property");
            String fromKindName = result.getString("from_kind");
            String fromProperty = result.getString("from_property");

            Reference reference = new Reference(new ReferenceKind(datasourceId, fromKindName, fromProperty), new ReferenceKind(datasourceId, kindName, toProperty));
            references.add(reference);
        }

        return references;
    }

    /**
     * Retrieves references for the specified kind.
     *
     * @param stmt         The {@link Statement} object used to execute the SQL query.
     * @param datasourceId ID of the datasource.
     * @param kindName     The name of the kind for which foreign key relationships are to be retrieved.
     * @return             A {@link List} of {@link Reference} objects representing the foreign key relationships for the specified kind.
     * @throws PullForestException if an error occurs during database access.
     */
    public static List<Reference> getReferences(Statement stmt, String datasourceId, String kindName) {
        try {
            List<Reference> foreignKeys = new ArrayList<>();
            foreignKeys = getReferences(stmt, foreignKeys, datasourceId, kindName, true);
            foreignKeys = getReferences(stmt, foreignKeys, datasourceId, kindName, false);

            return foreignKeys;
        }
        catch (SQLException e) {
            throw PullForestException.innerException(e);
        }
    }

    /**
     * Defines a mapping of comparison operator names to PostgreSQL operators.
     *
     * @return A {@link Map} containing operator names as keys and their PostgreSQL equivalents as values.
     */
    private static Map<String, String> defineOperators() {
        final var ops = new TreeMap<String, String>();
        ops.put("Equal", "=");
        ops.put("NotEqual", "<>");
        ops.put("Less", "<");
        ops.put("LessOrEqual", "<=");
        ops.put("Greater", ">");
        ops.put("GreaterOrEqual", ">=");

        ops.put("IsNull", "IS NULL");
        ops.put("IsNotNull", "IS NOT NULL");

        ops.put("Like", "LIKE");
        ops.put("ILike", "ILIKE");
        ops.put("NotLike", "NOT LIKE");
        ops.put("MatchRegEx", "~");
        ops.put("NotMatchRegEx", "!~");

        ops.put("In", "IN");
        ops.put("NotIn", "NOT IN");

        return ops;
    }

    /**
     * A map of operator names to PostgreSQL operators.
     *
     * @return A {@link Map} of operator names to PostgreSQL operators.
     */
    private static final Map<String, String> OPERATORS = defineOperators();

    /**
     * A list of PostgreSQL unary operators.
     *
     * @return A {@link List} of PostgreSQL unary operators.
     */
    private static final List<String> UNARY_OPERATORS = Arrays.asList("IS NULL", "IS NOT NULL");

    /**
     * A list of PostgreSQL operators used with string values.
     *
     * @return A {@link List} of PostgreSQL operators used with string values.
     */
    private static final List<String> STRING_OPERATORS = Arrays.asList("LIKE", "ILIKE", "NOT LIKE", "~", "!~");

    /**
     * Returns a map of operator names to PostgreSQL operators.
     *
     * @return A {@link Map} of operator names to PostgreSQL operators.
     */
    public Map<String, String> getOperators() {
        return OPERATORS;
    }

    /**
     * Returns a list of PostgreSQL unary operators.
     *
     * @return A {@link List} of PostgreSQL unary operators.
     */
    public List<String> getUnaryOperators() {
        return UNARY_OPERATORS;
    }

    /**
     * Returns a list of PostgreSQL operators used with string values.
     *
     * @return A {@link List} of PostgreSQL operators used with string values.
     */
    public List<String> getStringOperators() {
        return STRING_OPERATORS;
    }
}
