package cz.cuni.matfyz.wrapperpostgresql;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.exception.PullForestException;
import cz.cuni.matfyz.abstractwrappers.utils.PullQuery;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.SimpleProperty;
import cz.cuni.matfyz.core.mapping.StaticName;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.record.RootRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLPullWrapper implements AbstractPullWrapper {
    
    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLPullWrapper.class);

    private ConnectionProvider connectionProvider;
    
    public PostgreSQLPullWrapper(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    private PreparedStatement prepareStatement(Connection connection, PullQuery query) throws SQLException {
        if (query.hasStringContent())
            return connection.prepareStatement(query.getStringContent());

        var command = "SELECT * FROM " + query.getKindName();

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
            Connection connection = connectionProvider.getConnection();
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
            Connection connection = connectionProvider.getConnection();
            Statement statement = connection.createStatement();
        ) {
            try (ResultSet resultSet = statement.executeQuery(selectAll)) {
                var output = new StringBuilder();
                while (resultSet.next())
                    output.append(resultSet.getInt("number")).append("\n");

                return output.toString();
            }
        }
    }
}