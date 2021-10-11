/*
 * To change this license header, choose License HeaderesultSet in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.wrapperMongodb;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.ForestOfRecords;
import cz.cuni.matfyz.core.RecordData;
import cz.cuni.matfyz.core.RecordRoot;
import cz.cuni.matfyz.core.mapping.AccessPathProperty;

import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;

/**
 *
 * @author jachym.bartik
 */
public class MongoDBPullWrapper implements AbstractPullWrapper
{
    private ConnectionProvider connectionProvider; // TODO inject?
    
	public ForestOfRecords pullForest(String selectAll, AccessPath path)
    {
        // MongoCollection<Document> collection = database.getCollection("kindName TODO");

        // TODO how could be selectAll used here?
        
        return null;
    }

	public ForestOfRecords pullForest(String selectAll, AccessPath path, int limit, int offset)
    {
        // TODO
        return null;
    }
}

interface ConnectionProvider
{
    MongoDatabase getConnection();
}