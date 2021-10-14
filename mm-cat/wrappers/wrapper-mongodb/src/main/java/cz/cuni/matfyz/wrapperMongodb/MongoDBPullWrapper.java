package cz.cuni.matfyz.wrapperMongodb;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.AccessPathProperty;
import cz.cuni.matfyz.core.record.*;

import com.mongodb.client.*;
import java.util.Iterator;
import org.bson.Document;

/**
 *
 */
public class MongoDBPullWrapper implements AbstractPullWrapper
{
    private DatabaseProvider databaseProvider;
    
    public void injectDatabaseProvider(DatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
    }
    
    @Override
	public ForestOfRecords pullForest(String selectAll, AccessPath path)
    {
        return pullForest(selectAll, path, false, 0, 0);
    }

    @Override
    public ForestOfRecords pullForest(String selectAll, AccessPath path, int limit, int offset)
    {
        return pullForest(selectAll, path, true, limit, offset);
    }
    
	public ForestOfRecords pullForest(String selectAll, AccessPath path, boolean doLimitAndOffset, int limit, int offset)
    {
        // selectAll should be in the form of "database.getCollection("<kindName>");"
        var database = databaseProvider.getDatabase();
        String kindName = selectAll.substring("database.getCollection(\"".length(), selectAll.length() - "\");".length());
        MongoCollection<Document> collection = database.getCollection(kindName);
        Iterator<Document> iterator = collection.find().iterator();
        
        var forest = new ForestOfRecords();
        int index = 0;
        
        while (iterator.hasNext())
        {
            Document document = iterator.next();
            
            index++;
            if (doLimitAndOffset)
                if (index <= offset || index > limit)
                    continue;
                
            var record = new DataRecord();
            addChildrenToProperty(document, record);
            forest.addRecord(record);
            
            /*
            for (AccessPathProperty property : path.properties())
            {
                String name = "TODO it should be contained in property.name";
                String value = TODO extract property from document?
                //record.addSimpleProperty(name, value);
            
                TODO the path needs to be traversed as a tree so we can construct complex properties first
            }
            */

            forest.addRecord(record);
        }
        
        return forest;
    }
    
    private void addChildrenToProperty(Document document, ComplexProperty property)
    {
        document.forEach((key, value) -> {
            if (value instanceof Document documentValue)
            {
                var childProperty = property.addComplexProperty(key);
                addChildrenToProperty(documentValue, childProperty);
            }
            else
            {
                property.addSimpleProperty(key, value);
            }
        });
    }
}