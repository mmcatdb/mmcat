package cz.cuni.matfyz.wrappermongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * @author jachymb.bartik
 */
public class MongoDBDatabaseProvider implements DatabaseProvider {

    private MongoDBSettings settings;

    // The client itself handles connection pooling so there should be only one client (with given connection string) per application.
    // This also means that there should be at most one instance of this class so it should be cached somewhere.
    private MongoClient mongoClient;

    public MongoDBDatabaseProvider(MongoDBSettings settings) {
        this.settings = settings;
    }

    public MongoDatabase getDatabase() {
        if (mongoClient == null)
            mongoClient = MongoClients.create(settings.getConnectionString());

        return mongoClient.getDatabase(settings.getDatabase());
    }

}