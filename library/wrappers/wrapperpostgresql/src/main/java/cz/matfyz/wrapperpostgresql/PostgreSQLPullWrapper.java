package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.KindNameFilterQuery;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.adminer.AdminerFilter;
import cz.matfyz.core.adminer.TableResponse;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.querying.LeafResult;
import cz.matfyz.core.querying.ListResult;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgreSQLPullWrapper implements AbstractPullWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLPullWrapper.class);

    private final PostgreSQLProvider provider;

    public PostgreSQLPullWrapper(PostgreSQLProvider provider) {
        this.provider = provider;
    }

    private PreparedStatement prepareStatement(Connection connection, QueryContent query, boolean isCountQuery) throws SQLException {
        if (query instanceof final PostgreSQLQuery postgreSQLQuery) {
            final var statement = connection.prepareStatement(postgreSQLQuery.queryString);
            for (int i = 0; i < postgreSQLQuery.rawVariables.size(); i++)
                statement.setString(i + 1, postgreSQLQuery.rawVariables.get(i));
            return statement;
        }

        if (query instanceof final StringQuery stringQuery)
            return connection.prepareStatement(stringQuery.content);

        if (query instanceof final KindNameQuery kindNameQuery)
            return connection.prepareStatement(kindNameQueryToString(kindNameQuery, null, isCountQuery));

        if (query instanceof final KindNameFilterQuery kindNameFilterQuery)
            return connection.prepareStatement(kindNameQueryToString(kindNameFilterQuery.kindNameQuery, kindNameFilterQuery.getFilters(), isCountQuery));

        throw PullForestException.invalidQuery(this, query);
    }

    private String kindNameQueryToString(KindNameQuery query, @Nullable List<AdminerFilter> filters, boolean isCountQuery) {
        String columns = isCountQuery ? "COUNT(1)" : "*";
        // TODO escape all table names globally
        var command = "SELECT " + columns + " FROM " + "\"" + query.kindName + "\"";
        if (filters != null)
            command += createWhereClause(filters);
        // FIXME prevent sql injection
        if (!isCountQuery && query.hasLimit())
            command += "\nLIMIT " + query.getLimit();
        if (!isCountQuery && query.hasOffset())
            command += "\nOFFSET " + query.getOffset();
        command += ";";

        return command;
    }

    /**
     * Constructs a WHERE clause based on a list of filters.
     */
    private String createWhereClause(List<AdminerFilter> filters) {
        if (filters.isEmpty()) {
            return "";
        }

        StringBuilder whereClause = new StringBuilder("\nWHERE ");

        for (int i = 0; i < filters.size(); i++) {
            AdminerFilter filter = filters.get(i);
            String propertyName = filter.propertyName();

            if (i != 0)
                whereClause.append(" AND ");

            Double doubleValue = this.parseNumeric(filter.propertyValue());

            appendPropertyName(whereClause, propertyName, doubleValue);

            String operator = OPERATORS.get(filter.operator());
            appendOperator(whereClause, operator);

            appendPropertyValue(whereClause, filter.propertyValue(), operator, doubleValue);
        }

        return whereClause.toString();
    }

    /**
     * Parses a numeric value from a given string.
     *
     * @return the parsed {@code Double} value if valid, or {@code null} if the input is {@code null} or not a valid number
     */
    private Double parseNumeric(String str) {
        if (str == null)
            return null;

        try {
            return Double.parseDouble(str);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    private static void appendPropertyName(StringBuilder whereClause, String propertyName, Double doubleValue) {
        whereClause.append(propertyName);

        if (doubleValue != null)
            whereClause.append("::NUMERIC");
    }

    private static void appendOperator(StringBuilder whereClause, String operator) {
        whereClause
            .append(" ")
            .append(operator)
            .append(" ");
    }

    private static void appendPropertyValue(StringBuilder whereClause, String propertyValue, String operator, Double doubleValue) {
        if (operator.equals("IN") || operator.equals("NOT IN")) {
            whereClause
                .append("(")
                .append(propertyValue)
                .append(")");
        }
        else if (!UNARY_OPERATORS.contains(operator)) {
            if (doubleValue != null && !STRING_OPERATORS.contains(operator)) {
                whereClause.append(doubleValue);
            }
            else {
                whereClause
                    .append("'")
                    .append(propertyValue)
                    .append("'");
            }
        }
    }

    @Override public ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException {
        try (
            Connection connection = provider.getConnection();
            PreparedStatement statement = prepareStatement(connection, query, false);
        ) {
            LOGGER.debug("Execute PostgreSQL query:\n{}", statement);

            try (
                ResultSet resultSet = statement.executeQuery()
            ) {
                return innerPullForest(path, resultSet);
            }
        }
        catch (Exception e) {
            throw PullForestException.inner(e);
        }
    }

    private ForestOfRecords innerPullForest(ComplexProperty path, ResultSet resultSet) throws SQLException {
        final ForestOfRecords forest = new ForestOfRecords();
        final List<Column> columns = createColumns(resultSet, path);

        while (resultSet.next()) {
            final var rootRecord = new RootRecord();

            for (final Column column : columns) {
                final var property = column.property;
                final var value = resultSet.getString(column.index);

                if (property.name() instanceof DynamicName) {
                    rootRecord.addDynamicRecordWithValue(property, column.name, value);
                    continue;
                }

                rootRecord.addSimpleRecord(property.signature(), value);
            }

            forest.addRecord(rootRecord);
        }

        return forest;
    }

    private record Column(int index, String name, AccessPath property) {}

    private List<Column> createColumns(ResultSet resultSet, ComplexProperty path) throws SQLException {
        final var metadata = resultSet.getMetaData();
        final int count = metadata.getColumnCount();
        final List<Column> columns = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            final String name = metadata.getColumnName(i);
            final @Nullable AccessPath property = path.findSubpathByName(name);

            if (property != null)
                columns.add(new Column(i, name, property));
        }

        return columns;
    }

    public String readTableAsStringForTests(String kindName) throws SQLException {
        try (
            Connection connection = provider.getConnection();
            Statement statement = connection.createStatement();
        ) {
            try (
                ResultSet resultSet = statement.executeQuery("SELECT * FROM \"" + kindName + "\";")
            ) {
                final var sb = new StringBuilder();
                while (resultSet.next())
                    sb.append(resultSet.getString("number")).append("\n");

                return sb.toString();
            }
        }
    }

    // #region Querying

    @Override public QueryResult executeQuery(QueryStatement query) {
        final var columns = query.structure().children().stream().map(child -> child.name).toList();

        try (
            Connection connection = provider.getConnection();
            PreparedStatement statement = prepareStatement(connection, query.content(), false);
        ) {
            LOGGER.info("Execute PostgreSQL query:\n{}", statement);

            try (
                ResultSet resultSet = statement.executeQuery()
            ) {
                final var builder = new ListResult.TableBuilder();
                builder.addColumns(columns);

                while (resultSet.next()) {
                    final var values = new ArrayList<String>();
                    for (final var column : columns)
                        values.add(getValueForLeafResult(resultSet, column));

                    builder.addRow(values);
                }

                return new QueryResult(builder.build(), query.structure());
            }
        }
        catch (Exception e) {
            throw PullForestException.inner(e);
        }
    }

    private static String getValueForLeafResult(ResultSet resultSet, String column) throws SQLException {
        final var value = resultSet.getString(column);
        return switch (value) {
            case null -> LeafResult.NULL_STRING;
            case "t" -> LeafResult.TRUE_STRING;
            case "f" -> LeafResult.FALSE_STRING;
            default -> value;
        };
    }

    @Override public List<String> getKindNames() {
        try (
            Connection connection = provider.getConnection();
            Statement stmt = connection.createStatement();
        ) {
            final String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'public';";
            final ResultSet resultSet = stmt.executeQuery(query);
            final List<String> output = new ArrayList<>();

            while (resultSet.next())
                output.add(resultSet.getString(1));

            return output;
        }
        catch (Exception e) {
			throw PullForestException.inner(e);
		}
    }

    @Override public TableResponse getRecords(String kindName, @Nullable Integer limit, @Nullable Integer offset, @Nullable List<AdminerFilter> filters) {
        KindNameQuery kindNameQuery = new KindNameQuery(kindName, limit, offset);
        if (filters == null)
            return getQueryResult(kindNameQuery);

        return getQueryResult(new KindNameFilterQuery(kindNameQuery, filters));
    }

    @Override public List<Reference> getReferences(String datasourceId, String kindName) {
        try (
            Connection connection = provider.getConnection();
            Statement stmt = connection.createStatement();
        ) {
            return PostgreSQLUtils.getReferences(stmt, datasourceId, kindName);
        }
        catch (Exception e) {
			throw PullForestException.inner(e);
		}
    }

    @Override public TableResponse getQueryResult(QueryContent query) {
        try (
            Connection connection = provider.getConnection();
        ) {
            final List<List<String>> data = new ArrayList<>();

            final PreparedStatement stmt = prepareStatement(connection, query, false);
            final ResultSet resultSet = stmt.executeQuery();

            final ResultSetMetaData metaData = resultSet.getMetaData();
            final int columnCount = metaData.getColumnCount();

            final List<String> propertyNames = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++)
                propertyNames.add(metaData.getColumnName(i));

            long itemCount = 0;

            while (resultSet.next()) {
                final List<String> item = new ArrayList<>();

                for (int i = 1; i <= columnCount; i++)
                    item.add(resultSet.getString(i));

                data.add(item);
                itemCount++;
            }

            final PreparedStatement countStmt = prepareStatement(connection, query, true);
            if (!(query instanceof StringQuery)) {
                final ResultSet countResultSet = countStmt.executeQuery();
                if (countResultSet.next())
                    itemCount = countResultSet.getInt(1);
            }

            return new TableResponse(data, itemCount, propertyNames);
        }
        catch (Exception e) {
			throw PullForestException.inner(e);
		}
    }

    /**
     * Defines a mapping of comparison operator names to PostgreSQL operators.
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
     */
    private static final Map<String, String> OPERATORS = defineOperators();

    /**
     * A list of PostgreSQL unary operators.
     */
    private static final List<String> UNARY_OPERATORS = Arrays.asList("IS NULL", "IS NOT NULL");

    /**
     * A list of PostgreSQL operators used with string values.
     */
    private static final List<String> STRING_OPERATORS = Arrays.asList("LIKE", "ILIKE", "NOT LIKE", "~", "!~");

    // #endregion

}
