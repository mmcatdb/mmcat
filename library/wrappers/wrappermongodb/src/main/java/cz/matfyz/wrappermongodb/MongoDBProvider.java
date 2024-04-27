package cz.matfyz.wrappermongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MongoDBProvider {

    public final MongoDBSettings settings;

    // The client itself handles connection pooling so there should be only one client (with given connection string) per application.
    // This also means that there should be at most one instance of this class so it should be cached somewhere.
    private MongoClient mongoClient;

    public MongoDBProvider(MongoDBSettings settings) {
        this.settings = settings;
    }

    public MongoDatabase getDatabase() {
        if (mongoClient == null)
            mongoClient = MongoClients.create(settings.createConnectionString());

        return mongoClient.getDatabase(settings.database);
    }

    public record MongoDBSettings(
        String host,
        String port,
        String authenticationDatabase,
        String database,
        @Nullable String username,
        @Nullable String password,
        boolean isWritable,
        boolean isQueryable
    ) {
    
        public String createConnectionString() {
            final var builder = new StringBuilder()
                .append("mongodb://");
    
            if (username != null)
                builder
                    .append(username);
    
            if (password != null)
                builder
                    .append(":")
                    .append(password);
    
            builder
                .append("@")
                .append(host)
                .append(":")
                .append(port)
                .append("/")
                .append(database)
                .append("?authSource=")
                .append(authenticationDatabase);
    
            return builder.toString();
        }
    
    }

}
