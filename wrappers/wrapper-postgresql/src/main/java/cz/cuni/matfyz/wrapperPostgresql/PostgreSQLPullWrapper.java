package cz.cuni.matfyz.wrapperPostgresql;

import cz.cuni.matfyz.abstractWrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractWrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.record.*;

import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class PostgreSQLPullWrapper implements AbstractPullWrapper
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLPullWrapper.class);

    private ConnectionProvider connectionProvider;

    public void injectConnectionProvider(ConnectionProvider connectionProvider)
    {
        this.connectionProvider = connectionProvider;
    }

    private String createBasicCommand(PullWrapperOptions options)
    {
        if (options.hasCommand())
        {
            String command = options.getCommand();
            return command.endsWith(";") ? command.substring(0, command.length() - 1) : command;
        }
        
        return "SELECT * FROM \"" + options.getKindName() + "\""; // TODO prevent SQL injection
    }

    private PreparedStatement prepareStatement(Connection connection, PullWrapperOptions options) throws SQLException
    {
        String command = createBasicCommand(options);

        if (options.hasLimit())
            command += "\nLIMIT " + options.getLimit();

        if (options.hasOffset())
            command += "\nOFFSET " + options.getOffset();

        PreparedStatement statement = connection.prepareStatement(command);
        LOGGER.debug("SQL statement: " + statement);
        
        return statement;
    }

    @Override
	public ForestOfRecords pullForest(ComplexProperty path, PullWrapperOptions options) throws Exception
    {
        try (
            Connection connection = connectionProvider.getConnection();
            PreparedStatement statement = prepareStatement(connection, options);
        )
        {
            try (ResultSet resultSet = statement.executeQuery())
            {
                ForestOfRecords forest = new ForestOfRecords();
                
                while (resultSet.next())
                {
                    var record = new RootRecord();
                    
                    for (AccessPath subpath : path.subpaths())
                    {
                        if (subpath instanceof SimpleProperty simpleProperty && simpleProperty.name() instanceof StaticName staticName)
                        {
                            String name = staticName.getStringName();
                            String value = resultSet.getString(name);
                            record.addSimpleValueRecord(staticName.toRecordName(), simpleProperty.value().signature(), value);
                        }
                    }
                            
                    forest.addRecord(record);
                }
                
                resultSet.close();
                return forest;
            }
        }
        catch (SQLException exception)
        {
            LOGGER.error("PostgeSQL exception: ", exception);
            throw exception;
        }
    }

    public String readTableAsStringForTests(String selectAll) throws SQLException
    {
        try (
            Connection connection = connectionProvider.getConnection();
            Statement statement = connection.createStatement();
        )
        {
            try (ResultSet resultSet = statement.executeQuery(selectAll))
            {
                var output = new StringBuilder();
                while (resultSet.next())
                    output.append(resultSet.getInt("number")).append("\n");

                return output.toString();
            }
        }
        catch (SQLException exception)
        {
            LOGGER.error("Cannot create prepared statement or connection.", exception);
        }

        throw new SQLException();
    }
}