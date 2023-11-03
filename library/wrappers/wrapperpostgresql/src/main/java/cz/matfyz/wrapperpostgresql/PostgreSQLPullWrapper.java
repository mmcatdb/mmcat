package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.queryresult.ResultList;
import cz.matfyz.abstractwrappers.queryresult.QueryResult;
import cz.matfyz.abstractwrappers.utils.PullQuery;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.RootRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLPullWrapper implements AbstractPullWrapper {
    
    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLPullWrapper.class);

    private PostgreSQLProvider provider;
    
    public PostgreSQLPullWrapper(PostgreSQLProvider provider) {
        this.provider = provider;
    }

    private PreparedStatement prepareStatement(Connection connection, PullQuery query) throws SQLException {
        if (query.hasStringContent())
            return connection.prepareStatement(query.getStringContent());

        // TODO escape all table names globally
        var command = "SELECT * FROM " + "\"" + query.getKindName() + "\"";

        if (query.hasLimit())
            command += "\nLIMIT " + query.getLimit();

        if (query.hasOffset())
            command += "\nOFFSET " + query.getOffset();

        command += ";";

        return connection.prepareStatement(command);
    }

    @Override
    public ForestOfRecords pullForest(ComplexProperty path, PullQuery query) throws PullForestException {
        try (
            Connection connection = provider.getConnection();
            PreparedStatement statement = prepareStatement(connection, query);
        ) {
            LOGGER.info("Execute PostgreSQL query:\n{}", statement);

            try (ResultSet resultSet = statement.executeQuery()) {
                ForestOfRecords forest = new ForestOfRecords();
                
                while (resultSet.next()) {
                    var rootRecord = new RootRecord();
                    
                    for (AccessPath subpath : path.subpaths()) {
                        if (subpath instanceof SimpleProperty simpleProperty && simpleProperty.name() instanceof StaticName staticName) {
                            String name = staticName.getStringName();
                            String value = resultSet.getString(name);
                            rootRecord.addSimpleValueRecord(staticName.toRecordName(), simpleProperty.signature(), value);
                        }
                    }
                            
                    forest.addRecord(rootRecord);
                }
                
                return forest;
            }
        }
        catch (Exception e) {
            throw new PullForestException(e);
        }
    }

    public String readTableAsStringForTests(String selectAll) throws SQLException {
        try (
            Connection connection = provider.getConnection();
            Statement statement = connection.createStatement();
        ) {
            try (ResultSet resultSet = statement.executeQuery(selectAll)) {
                var output = new StringBuilder();
                while (resultSet.next())
                    output.append(resultSet.getString("number")).append("\n");

                return output.toString();
            }
        }
    }

    @Override
    public QueryResult executeQuery(QueryStatement query) {
        final var columns = query.structure().children.values().stream().map(child -> child.name).toList();

        try (
            Connection connection = provider.getConnection();
            PreparedStatement statement = connection.prepareStatement(query.stringContent());
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
            throw new PullForestException(e);
        }
    }

}