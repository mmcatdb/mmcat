package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.core.adminer.AdminerFilter;
import cz.matfyz.core.adminer.DataResponse;
import cz.matfyz.core.adminer.KindNamesResponse;
import cz.matfyz.core.adminer.TableResponse;
import cz.matfyz.core.adminer.Reference;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.DynamicName;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.querying.queryresult.ResultList;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        try (
            Connection connection = provider.getConnection();
            PreparedStatement statement = prepareStatement(connection, query);
        ) {
            LOGGER.debug("Execute PostgreSQL query:\n{}", statement);

            try (
                ResultSet resultSet = statement.executeQuery()
            ) {
                return innerPullForest(path, resultSet);
            }
        }
        catch (Exception e) {
            throw PullForestException.innerException(e);
        }
    }

    private ForestOfRecords innerPullForest(ComplexProperty path, ResultSet resultSet) throws SQLException {
        final ForestOfRecords forest = new ForestOfRecords();
        final List<Column> columns = createColumns(resultSet, path);
        final var replacedNames = path.copyWithoutDynamicNames().replacedNames();

        while (resultSet.next()) {
            final var rootRecord = new RootRecord();

            for (final Column column : columns) {
                final var value = resultSet.getString(column.index);

                if (!(column.property.name() instanceof final DynamicName dynamicName)) {
                    rootRecord.addSimpleRecord(column.property.signature(), value);
                    continue;
                }

                final var replacement = replacedNames.get(dynamicName);
                final var replacer = rootRecord.addDynamicReplacer(replacement.prefix(), replacement.name(), column.name);
                replacer.addSimpleRecord(replacement.value().signature(), value);
            }

            forest.addRecord(rootRecord);
        }

        return forest;
    }

    private record Column(int index, String name, SimpleProperty property) {}

    private List<Column> createColumns(ResultSet resultSet, ComplexProperty path) throws SQLException {
        final var metadata = resultSet.getMetaData();
        final int count = metadata.getColumnCount();
        final List<Column> columns = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            final String name = metadata.getColumnName(i);
            final @Nullable AccessPath property = path.findSubpathByName(name);

            if (property != null)
                columns.add(new Column(i, name, (SimpleProperty) property));
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
     * @return A {@link KindNamesResponse} containing a list of kind names.
     * @throws PullForestException if an error occurs during database access.
     */
    @Override public KindNamesResponse getKindNames(String limit, String offset) {
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

            return new KindNamesResponse(data);
        }
        catch (Exception e) {
			throw PullForestException.innerException(e);
		}
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

            if (!whereClause.isEmpty()) {
                whereClause = "WHERE " + whereClause;
            }

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

            Set<String> propertyNames = PostgreSQLUtils.getPropertyNames(stmt, kindName);

            return new TableResponse(data, itemCount, propertyNames);
        }
        catch (Exception e) {
			throw PullForestException.innerException(e);
		}
    }

    /**
     * Retrieves a list of references for a specified kind.
     *
     * @param datasourceId ID of the datasource.
     * @param kindName     The name of the kind for which references are being retrieved.
     * @return A {@link List} of {@link Reference} objects representing the references of the kind.
     * @throws PullForestException if an error occurs during database access.
     */
    @Override public List<Reference> getReferences(String datasourceId, String kindName) {
        try(
            Connection connection = provider.getConnection();
            Statement stmt = connection.createStatement();
        ){
            return PostgreSQLUtils.getReferences(stmt, datasourceId, kindName);
        }
        catch (Exception e) {
			throw PullForestException.innerException(e);
		}
    }

    /**
     * Retrieves the result of the given query.
     *
     * @param query the custom query.
     * @return a {@link DataResponse} containing the data result of custom query.
     */
    @Override public DataResponse getQueryResult(String query) {
        try(
            Connection connection = provider.getConnection();
            Statement stmt = connection.createStatement();
        ){
            List<Map<String, String>> data = new ArrayList<>();

            ResultSet resultSet = stmt.executeQuery(query);

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            int itemCount = 0;

            while (resultSet.next()) {
                Map<String, String> item = new HashMap<>();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnValue = resultSet.getString(i);

                    item.put(columnName, columnValue);
                }

                data.add(item);
                itemCount++;
            }

            return new TableResponse(data, itemCount, null);
        }
        catch (Exception e) {
			throw PullForestException.innerException(e);
		}
    }

    /**
     * Constructs a WHERE clause based on a list of filters.
     *
     * @param filters The filters to apply.
     * @return A WHERE clause as a {@link String}.
     */
    private String createWhereClause(List<AdminerFilter> filters) {
        if ((filters == null || filters.isEmpty())) {
            return "";
        }

        StringBuilder whereClause = new StringBuilder();

        for (int i = 0; i < filters.size(); i++) {
            AdminerFilter filter = filters.get(i);
            String propertyName = filter.propertyName();

            if (i != 0) {
                whereClause.append(" AND ");
            }

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
     * If the string represents a valid number, it returns the parsed {@code Double}.
     * Otherwise, it returns {@code null}.
     *
     * @param str the string to be parsed
     * @return the parsed {@code Double} value if valid, or {@code null} if the input is {@code null} or not a valid number
     */
    private Double parseNumeric(String str) {
        if (str == null) {
            return null;
        }

        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void appendPropertyName(StringBuilder whereClause, String propertyName, Double doubleValue) {
        whereClause.append(propertyName);

        if (doubleValue != null) {
            whereClause.append("::NUMERIC");
        }
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
        } else if (!UNARY_OPERATORS.contains(operator)) {
            if (doubleValue != null && !STRING_OPERATORS.contains(operator)) {
                whereClause
                    .append(doubleValue);
            } else {
                whereClause
                    .append("'")
                    .append(propertyValue)
                    .append("'");
            }
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

}
