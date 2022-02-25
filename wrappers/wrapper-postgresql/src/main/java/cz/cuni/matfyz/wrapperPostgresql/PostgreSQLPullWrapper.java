package cz.cuni.matfyz.wrapperPostgresql;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.record.*;

import java.sql.*;

/**
 *
 * @author jachymb.bartik
 */
public class PostgreSQLPullWrapper implements AbstractPullWrapper
{
    private ConnectionProvider connectionProvider;

    public void injectConnectionProvider(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }
    
    @Override
	public ForestOfRecords pullForest(String selectAll, ComplexProperty path) throws Exception
    {
        /*
        ResultSet resultSet = getData(selectAll);
        if (resultSet == null)
            return null;
        */
        
        try (
            Connection connection = connectionProvider.getConnection();
            Statement statement = connection.createStatement();
        )
        {
            try (ResultSet resultSet = statement.executeQuery(selectAll))
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
            System.err.println("Can't get result: " + exception.getMessage());
        }
        
        return null;
    }

    @Override
	public ForestOfRecords pullForest(String selectAll, ComplexProperty path, int limit, int offset) throws Exception
    {
        String newSelectAll = String.format("%s\nLIMIT %d\nOFFSET %d", selectAll, limit, offset);
        return pullForest(newSelectAll, path);
    }
    
/*
    private ResultSet getData(String command)
    {
        Connection connection = connectionProvider.getConnection();
        try
        {
//            connection.setAutoCommit(false);

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(command);

            statement.close();
            connection.close();
           
            return resultSet;
        }
        catch (SQLException exception)
        {
            System.err.println("Can't get result: " + exception.getMessage());
        }
        
        return null;
    }
*/
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
            System.err.println("Can't get result: " + exception.getMessage());
        }

        throw new SQLException();
    }
}