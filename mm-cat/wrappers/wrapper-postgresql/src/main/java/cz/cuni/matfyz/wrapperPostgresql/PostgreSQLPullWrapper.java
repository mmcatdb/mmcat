package cz.cuni.matfyz.wrapperPostgresql;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.AccessPathProperty;
import cz.cuni.matfyz.core.record.*;

import java.sql.*;

/**
 *
 */
public class PostgreSQLPullWrapper implements AbstractPullWrapper
{
    private ConnectionProvider connectionProvider;

    public void injectConnectionProvider(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }
    
    @Override
	public ForestOfRecords pullForest(String selectAll, AccessPath path)
    {
        ResultSet resultSet = getData(selectAll);
        if (resultSet == null)
            return null;
        
        try
        {
            ForestOfRecords forest = new ForestOfRecords();
            
            while (resultSet.next())
            {
                var record = new DataRecord();
                
                for (AccessPathProperty property : path.properties())
                {
                    String name = "TODO it should be contained in property.name";
                    String value = resultSet.getString(name);
                    record.addSimpleProperty(name, value);
                }
                
                forest.addRecord(record);
            }
            
            resultSet.close();
            return forest;
        }
        catch (SQLException exception)
        {
            System.err.println("Can't get result: " + exception.getMessage());
        }
        
        return null;
    }

    @Override
	public ForestOfRecords pullForest(String selectAll, AccessPath path, int limit, int offset)
    {
        String newSelectAll = String.format("%s\nLIMIT %d\nOFFSET %d", selectAll, limit, offset);
        return pullForest(newSelectAll, path);
    }
    
    private ResultSet getData(String command)
    {
        Connection connection = connectionProvider.getConnection();
        try
        {
            connection.setAutoCommit(false);

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
}