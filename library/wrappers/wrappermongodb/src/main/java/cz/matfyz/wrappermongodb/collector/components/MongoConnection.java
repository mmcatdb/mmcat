package cz.matfyz.wrappermongodb.collector.components;

import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;

import cz.matfyz.wrappermongodb.collector.MongoExceptionsFactory;
import cz.matfyz.abstractwrappers.exception.collector.QueryExecutionException;
import org.bson.Document;

public record MongoConnection(MongoDatabase database) {
    /**
     * Implementation of abstract method that will execute query and cache whole result parsed to CachedResult
     * @param query inputted query
     * @return instance of CachedResult which corresponds to native result of inputted query
     * @throws QueryExecutionException when some MongoException or ParseException occur during process
     */
    public Document executeQuery(Document query) throws QueryExecutionException {
        try {
            return database.runCommand(query);
        } catch (MongoException e) {
            throw MongoExceptionsFactory.getExceptionsFactory().queryExecutionFailed(e);
        }
    }
}
