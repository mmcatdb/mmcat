package cz.matfyz.wrappermongodb.collector.components;

import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import cz.matfyz.abstractwrappers.collector.components.AbstractConnection;

import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;
import cz.matfyz.wrappermongodb.collector.MongoResources;
import cz.matfyz.abstractwrappers.exception.collector.QueryExecutionException;
import org.bson.Document;

/**
 * Mongodb implementation of AbstractConnection
 */
public class MongoConnection extends AbstractConnection<Document, Document, Document> {

    /**
     * Field containing reference to mongodb database over which is wrapper working
     */
    MongoDatabase _database;
    public MongoConnection(MongoDatabase database, WrapperExceptionsFactory exceptionsFactory) {
        super(exceptionsFactory);
        _database = database;
    }

    /**
     * Implementation of abstract method that will execute query and cache whole result parsed to CachedResult
     * @param query inputted query
     * @return instance of CachedResult which corresponds to native result of inputted query
     * @throws QueryExecutionException when some MongoException or ParseException occur during process
     */
    @Override
    public Document executeQuery(Document query) throws QueryExecutionException {
        try {
            return _database.runCommand(query);
        } catch (MongoException e) {
            throw getExceptionsFactory().queryExecutionFailed(e);
        }
    }

    @Override
    public ResultWithPlan<Document, Document> executeWithExplain(Document query) throws QueryExecutionException {
        try {
            Document result = _database.runCommand(query);
            Document plan = _database.runCommand(MongoResources.getExplainCommand(query));
            return new ResultWithPlan<>(result, plan);
        } catch (MongoException e) {
            throw getExceptionsFactory().queryExecutionWithExplainFailed(e);
        }
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    /**
     * Method which implements AutoClosable interface
     */
    @Override
    public void close() {}
}
