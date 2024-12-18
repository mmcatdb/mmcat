package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.adminer.KindNameResponse;
import cz.matfyz.core.adminer.TableResponse;
import cz.matfyz.core.adminer.ForeignKey;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.querying.queryresult.ResultList;
import cz.matfyz.core.record.AdminerFilter;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.checkerframework.checker.nullness.qual.Nullable;

public class PostgreSQLPullWrapper implements AbstractPullWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLPullWrapper.class);

    private PostgreSQLProvider provider;

    public PostgreSQLPullWrapper(PostgreSQLProvider provider) {
        this.provider = provider;
    }

    private PreparedStatement prepareStatement(Connection connection, QueryContent query) throws SQLException {
        if (query instanceof final StringQuery stringQuery)
            return connection.prepareStatement(stringQuery.content);

        if (query instanceof final KindNameQuery kindNameQuery)
            return connection.prepareStatement(kindNameQueryToString(kindNameQuery));

        throw PullForestException.invalidQuery(this, query);
    }

    private String kindNameQueryToString(KindNameQuery query) {
        // TODO escape all table names globally
        var command = "SELECT * FROM " + "\"" + query.kindName + "\"";
        if (query.hasLimit())
            command += "\nLIMIT " + query.getLimit();
        if (query.hasOffset())
            command += "\nOFFSET " + query.getOffset();
        command += ";";

        return command;
    }

    @Override public ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException {
        System.out.println("pullForest from postgres");
        try (
            Connection connection = provider.getConnection();
            PreparedStatement statement = prepareStatement(connection, query);
        ) {
            LOGGER.info("Execute PostgreSQL query:\n{}", statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                final ForestOfRecords forest = new ForestOfRecords();

                while (resultSet.next()) {
                    final var rootRecord = new RootRecord();

                    for (final AccessPath subpath : path.subpaths()) {
                        if (subpath instanceof final SimpleProperty simpleProperty && simpleProperty.name() instanceof StaticName staticName) {
                            final String name = staticName.getStringName();
                            final String value = resultSet.getString(name);
                            rootRecord.addSimpleValueRecord(staticName.toRecordName(), simpleProperty.signature(), value);
                        }
                    }

                    forest.addRecord(rootRecord);
                }

                return forest;
            }
        }
        catch (Exception e) {
            throw PullForestException.innerException(e);
        }
    }

    public String readTableAsStringForTests(String kindName) throws SQLException {
        try (
            Connection connection = provider.getConnection();
            Statement statement = connection.createStatement();
        ) {
            try (
                ResultSet resultSet = statement.executeQuery("SELECT * FROM \"" + kindName + "\";")
            ) {
                final var output = new StringBuilder();
                while (resultSet.next())
                    output.append(resultSet.getString("number")).append("\n");

                return output.toString();
            }
        }
    }

    @Override public QueryResult executeQuery(QueryStatement query) {
        final var columns = query.structure().children().stream().map(child -> child.name).toList();

        try (
            Connection connection = provider.getConnection();
            PreparedStatement statement = prepareStatement(connection, query.content());
        ) {
            LOGGER.info("Execute PostgreSQL query:\n{}", statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                final var builder = new ResultList.TableBuilder();
                builder.addColumns(columns);

                while (resultSet.next()) {
                    final var values = new ArrayList<String>();
                    for (final var column : columns)
                        values.add(resultSet.getString(column));

                    builder.addRow(values);
                }

                return new QueryResult(builder.build(), query.structure());
            }
        }
        catch (Exception e) {
            throw PullForestException.innerException(e);
        }
    }

    @Override public KindNameResponse getKindNames(String limit, String offset) {
        try(
            Connection connection = provider.getConnection();
            Statement stmt = connection.createStatement();
        ){
            final String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'public' LIMIT " + limit + " OFFSET " + offset + ";";
            ResultSet resultSet = stmt.executeQuery(query);
            List<String> data = new ArrayList<>();

            while (resultSet.next()) {
                String kindName = resultSet.getString(1);
                data.add(kindName);
            }

            return new KindNameResponse(data);
        }
        catch (Exception e) {
			throw PullForestException.innerException(e);
		}
    }

    private Set<String> getColumnNames(Statement stmt, String kindName) throws SQLException {
        Set<String> columns = new HashSet<>();

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
            columns.add(column);
        }

        return columns;
    }

    private String createWhereClause(List<AdminerFilter> filters) {
        StringBuilder whereClause = new StringBuilder();

        if (filters == null || filters.isEmpty()) {
            return "";
        }

        for (int i = 0; i < filters.size(); i++) {
            AdminerFilter filter = filters.get(i);

            if (i == 0) {
                whereClause.append("WHERE ");
            } else {
                whereClause.append("AND ");
            }

            whereClause.append(filter.columnName())
                       .append(" ")
                       .append(filter.operator())
                       .append(" '")
                       .append(filter.columnValue())
                       .append("' ");
        }

        return whereClause.toString();
    }

    @Override public TableResponse getKind(String kindName, String limit, String offset, @Nullable List<AdminerFilter> filter) {
        try(
            Connection connection = provider.getConnection();
            Statement stmt = connection.createStatement();
        ){
            List<Map<String, String>> data = new ArrayList<>();

            String whereClause = createWhereClause(filter);
            String selectQuery = "SELECT * FROM \"" + kindName +  "\" " + whereClause + " LIMIT " + limit + " OFFSET " + offset + ";";
            ResultSet resultSet = stmt.executeQuery(selectQuery);

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, String> item = new HashMap<>();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnValue = resultSet.getString(i);

                    item.put(columnName, columnValue);
                }

                data.add(item);
            }

            int itemCount = 0;
            String countQuery = "SELECT COUNT(*) FROM \"" + kindName +  "\" " + whereClause + ";";

            ResultSet countResultSet = stmt.executeQuery(countQuery);
            if (countResultSet.next()) {
                itemCount = countResultSet.getInt(1);
            }

            Set<String> propertyNames = getColumnNames(stmt, kindName);

            return new TableResponse(data, itemCount, propertyNames);
        }
        catch (Exception e) {
			throw PullForestException.innerException(e);
		}
    }

    /**
     * Retrieves foreign key relationships from the database for the specified kind.
     *
     * @param stmt       The {@link Statement} object used to execute the SQL query.
     * @param keys       A {@link List} of {@link ForeignKey} objects to which the results will be added.
     * @param kindName   The name of the kind for which foreign key relationships are to be retrieved.
     * @param outgoing   A boolean flag indicating the direction of the foreign key relationship:
     *                   <ul>
     *                       <li><code>true</code> for outgoing foreign keys (keys where the kind references other kinds).</li>
     *                       <li><code>false</code> for incoming foreign keys (keys where other kinds reference the kind).</li>
     *                   </ul>
     * @return           A {@link List} of {@link ForeignKey} objects representing the foreign key relationships for the specified kind.
     * @throws SQLException If an SQL error occurs while executing the query or processing the result set.
     */
    private List<ForeignKey> getForeignKeysFromDatabase(Statement stmt, List<ForeignKey> keys, String kindName, boolean outgoing) throws SQLException {
        String actualKind = outgoing ? "kcu" : "ccu";
        String foreignKind = outgoing ? "ccu" : "kcu";

        String query = String.format("""
                SELECT
                    %s.column_name AS column,
                    %s.table_name AS foreign_table,
                    %s.column_name AS foreign_column
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
                """, actualKind, foreignKind, foreignKind, actualKind, kindName);
        ResultSet result = stmt.executeQuery(query);

        while (result.next()) {
            String column = result.getString("column");
            String foreignTable = result.getString("foreign_table");
            String foreignColumn = result.getString("foreign_column");

            ForeignKey foreignKey = new ForeignKey(foreignTable, column, foreignColumn);
            keys.add(foreignKey);
        }

        return keys;
    }

    @Override public List<ForeignKey> getForeignKeys(String kindName) {
        try(
            Connection connection = provider.getConnection();
            Statement stmt = connection.createStatement();
        ){
            List<ForeignKey> foreignKeys = new ArrayList<>();
            foreignKeys = getForeignKeysFromDatabase(stmt, foreignKeys, kindName, true);
            foreignKeys = getForeignKeysFromDatabase(stmt, foreignKeys, kindName, false);

            return foreignKeys;
        }
        catch (Exception e) {
			throw PullForestException.innerException(e);
		}
    }
}
