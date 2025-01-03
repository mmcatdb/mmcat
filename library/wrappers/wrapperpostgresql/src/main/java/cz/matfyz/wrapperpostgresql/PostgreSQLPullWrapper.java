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
import cz.matfyz.inference.adminer.PostgreSQLAlgorithms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    /**
     * Retrieves a list of kind names from the database.
     *
     * @param limit  The maximum number of results to return.
     * @param offset The starting position of the result set.
     * @return A {@link KindNameResponse} containing a list of kind names.
     * @throws PullForestException if an error occurs during database access.
     */
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

    /**
     * Creates a PostgreSQL WHERE clause from a list of filters.
     *
     * @param filters The list of {@link AdminerFilter} objects representing filter conditions.
     * @return A {@link String} containing the WHERE clause, or an empty string if no filters are provided.
     */
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

            String operator = PostgreSQLAlgorithms.OPERATORS.get(filter.operator());

            whereClause.append(filter.columnName())
                .append(" ")
                .append(PostgreSQLAlgorithms.OPERATORS.get(filter.operator()));

            if (operator.equals("IN") || operator.equals("NOT IN")) {
                whereClause
                    .append(" ")
                    .append(Arrays.stream(filter.columnValue().split(";"))
                        .map(String::trim)
                        .map(value -> "'" + value + "'")
                        .collect(Collectors.joining(", ", "(", ")")))
                    .append("");
            } else if (!PostgreSQLAlgorithms.UNARY_OPERATORS.contains(operator)) {
                whereClause
                    .append(" '")
                    .append(filter.columnValue())
                    .append("'");
            }
        }

        return whereClause.toString();
    }

    /**
     * Retrieves data for a specific kind with optional filtering, pagination, and sorting.
     *
     * @param kindName The name of the kind to query.
     * @param limit    The maximum number of rows to return.
     * @param offset   The starting position of the result set.
     * @param filter   A list of {@link AdminerFilter} objects representing filter conditions (optional).
     * @return A {@link TableResponse} containing the kind data, total row count, and column names.
     * @throws PullForestException if an error occurs during database access.
     */
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

            Set<String> propertyNames = PostgreSQLAlgorithms.getPropertyNames(stmt, kindName);

            return new TableResponse(data, itemCount, propertyNames);
        }
        catch (Exception e) {
			throw PullForestException.innerException(e);
		}
    }

    /**
     * Retrieves a list of foreign keys for a specified kind.
     *
     * The method fetches both inbound (referenced by other kinds) and outbound
     * (referencing other kinds) foreign keys associated with the given kind name.
     *
     * @param kindName The name of the kind for which foreign keys are being retrieved.
     * @return A {@link List} of {@link ForeignKey} objects representing the foreign keys of the kind.
     * @throws PullForestException if an error occurs during database access.
     */
    @Override public List<ForeignKey> getForeignKeys(String kindName) {
        try(
            Connection connection = provider.getConnection();
            Statement stmt = connection.createStatement();
        ){
            return PostgreSQLAlgorithms.getForeignKeys(stmt, kindName);
        }
        catch (Exception e) {
			throw PullForestException.innerException(e);
		}
    }
}
