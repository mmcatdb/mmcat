/*
 * To change this license header, choose License HeaderesultSet in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.wrapperPostgresql;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.ForestOfRecords;
import cz.cuni.matfyz.core.RecordData;
import cz.cuni.matfyz.core.RecordRoot;
import cz.cuni.matfyz.core.mapping.AccessPathProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author jachym.bartik
 */
public class PostgreSQLPullWrapper implements AbstractPullWrapper
{
    private ConnectionProvider connectionProvider; // TODO inject?
    
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
                ArrayList<RecordData> data = new ArrayList<RecordData>();
                for (AccessPathProperty property: path.properties())
                {
                    String propertyName = "TODO it should be contained in property.name";
                    String value = resultSet.getString(propertyName);
                    
                    // TODO create RecordData from name and value?
                    data.add(new RecordData());
                }
                
                // TODO add record data to the root of the tree?
                RecordRoot tree = new RecordRoot();
                
                // TODO add tree to the forest?
            }
            
            resultSet.close();
            
            return forest;
        }
        catch (Exception exception)
        {
            System.err.println("Can't get result: " + exception.getMessage());
        }
        
        return null;
    }

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
        catch (Exception exception)
        {
            System.err.println("Can't get result: " + exception.getMessage());
        }
        
        return null;
    }
}

interface ConnectionProvider
{
    Connection getConnection();
}