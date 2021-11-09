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
        ResultSet resultSet = getData(selectAll);
        if (resultSet == null)
            return null;
        
        try
        {
            ForestOfRecords forest = new ForestOfRecords();
            
            while (resultSet.next())
            {
                var record = new DataRecord();
                
                for (AccessPath subpath : path.subpaths())
                {
                    String name = subpath.name().getStringName();
                    String value = resultSet.getString(name);
                    record.addSimpleRecord(subpath.name().toRecordName(), value);
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
	public ForestOfRecords pullForest(String selectAll, ComplexProperty path, int limit, int offset) throws Exception
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