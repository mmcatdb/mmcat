package cz.cuni.matfyz.wrapperMongodb;

import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.record.*;

import com.mongodb.client.*;
import java.util.Iterator;
import org.bson.Document;

/**
 *
 * @author jachymb.bartik
 */
public class MongoDBPullWrapper implements AbstractPullWrapper
{
    private DatabaseProvider databaseProvider;
    
    public void injectDatabaseProvider(DatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
    }
    
    @Override
	public ForestOfRecords pullForest(String selectAll, ComplexProperty path) throws Exception
    {
        return pullForest(selectAll, path, false, 0, 0);
    }

    @Override
    public ForestOfRecords pullForest(String selectAll, ComplexProperty path, int limit, int offset) throws Exception
    {
        return pullForest(selectAll, path, true, limit, offset);
    }
    
	public ForestOfRecords pullForest(String selectAll, ComplexProperty path, boolean doLimitAndOffset, int limit, int offset) throws Exception
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
            processPath(path, record, document);
            forest.addRecord(record);
        }
        
        return forest;
    }
    
    private void processPath(AccessPath path, ComplexRecord record, Document document) throws Exception
    {
        String stringName = path.name().getStringName();
        Object value = (document == null || !document.containsKey(stringName)) ? null : document.get(stringName);

        if (path instanceof ComplexProperty innerNode)
            processNode(innerNode, record, value);
        else if (path instanceof SimpleProperty leafNode)
            processNode(leafNode, record, value);
    }
    
    private void processNode(ComplexProperty innerNode, ComplexRecord record, Object value) throws Exception
    {
        for (AccessPath subpath : innerNode.subpaths())
        {
            ComplexRecord childRecord = record.addComplexRecord(innerNode.name().toRecordName());
            Document childDocument = value instanceof Document documentValue ? documentValue : null;
            processPath(subpath, childRecord, childDocument);
        }
    }
    
    private void processNode(SimpleProperty leafNode, ComplexRecord record, Object value) throws Exception
    {
        record.addSimpleRecord(leafNode.name().toRecordName(), value);
    }
}