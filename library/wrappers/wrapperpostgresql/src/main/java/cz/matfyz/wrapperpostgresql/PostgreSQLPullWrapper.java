package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.KindNameQuery;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

}
