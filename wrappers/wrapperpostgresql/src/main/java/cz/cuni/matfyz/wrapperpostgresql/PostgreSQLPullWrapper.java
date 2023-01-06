package cz.cuni.matfyz.wrapperpostgresql;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.PullWrapperOptions;
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
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLPullWrapper.class);

    private ConnectionProvider connectionProvider;

    public void injectConnectionProvider(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    private PreparedStatement prepareStatement(Connection connection, PullWrapperOptions options) throws SQLException {
        // Prevent SQL injection.
        if (!options.getKindName().matches("^[_a-zA-Z0-9.]+$"))
            throw new SQLException("Invalid table name.");

        String command = "SELECT * FROM " + options.getKindName();

        if (options.hasLimit())
            command += "\nLIMIT " + options.getLimit();

        if (options.hasOffset())
            command += "\nOFFSET " + options.getOffset();

        command += ";";

        return connection.prepareStatement(command);
    }

    @Override
    public ForestOfRecords pullForest(ComplexProperty path, PullWrapperOptions options) throws Exception {
        try (
            Connection connection = connectionProvider.getConnection();
            PreparedStatement statement = prepareStatement(connection, options);
        ) {
            try (ResultSet resultSet = statement.executeQuery()) {
                ForestOfRecords forest = new ForestOfRecords();
                
                while (resultSet.next()) {
                    var rootRecord = new RootRecord();
                    
                    for (AccessPath subpath : path.subpaths()) {
                        if (subpath instanceof SimpleProperty simpleProperty && simpleProperty.name() instanceof StaticName staticName) {
                            String name = staticName.getStringName();
                            String value = resultSet.getString(name);
                            rootRecord.addSimpleValueRecord(staticName.toRecordName(), simpleProperty.value().signature(), value);
                        }
                    }
                            
                    forest.addRecord(rootRecord);
                }

                resultSet.close();
                
                return forest;
            }
        }
        catch (SQLException exception) {
            LOGGER.error("PostgeSQL exception: ", exception);
            throw exception;
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
        catch (SQLException exception) {
            LOGGER.error("Cannot create prepared statement or connection.", exception);
        }

        throw new SQLException();
    }
}