package cz.matfyz.wrapperpostgresql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.adminer.Reference.ReferenceKind;

public class PostgreSQLUtils {

    private PostgreSQLUtils() {}

    /**
     * Retrieves foreign key relationships from the database for the specified kind.
     *
     * @param references    The list of references to which the foreign keys will be added.
     * @param outgoing      A boolean flag indicating the direction of the foreign key relationship:
     *                   <ul>
     *                       <li><code>true</code> for outgoing foreign keys (keys where the kind references other kinds).</li>
     *                       <li><code>false</code> for incoming foreign keys (keys where other kinds reference the kind).</li>
     *                   </ul>
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
     */
    public static List<Reference> getReferences(Statement stmt, String datasourceId, String kindName) {
        try {
            List<Reference> foreignKeys = new ArrayList<>();
            foreignKeys = getReferences(stmt, foreignKeys, datasourceId, kindName, true);
            foreignKeys = getReferences(stmt, foreignKeys, datasourceId, kindName, false);

            return foreignKeys;
        }
        catch (SQLException e) {
            throw PullForestException.inner(e);
        }
    }

}
