package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractDatasourceProvider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MongoDBProvider implements AbstractDatasourceProvider {

    public final MongoDBSettings settings;

    // The client itself handles connection pooling so there should be only one client (with given connection string) per application.
    // This also means that there should be at most one instance of this class so it should be cached somewhere.
    private MongoClient mongoClient;

    private MongoClient noUserMongoClient;

    private static final String ADMIN_NAME = "admin";

    public MongoDBProvider(MongoDBSettings settings) {
        this.settings = settings;
    }

    public MongoDatabase getDatabase() {
        if (mongoClient == null)
            mongoClient = MongoClients.create(settings.createConnectionString());

        return mongoClient.getDatabase(settings.database);
    }

    public MongoDatabase getNoUserDatabase() {
        if (noUserMongoClient == null)
            noUserMongoClient = MongoClients.create(settings.createNoUserConnectionString());

        return noUserMongoClient.getDatabase(ADMIN_NAME);
    }

    public boolean isStillValid(Object settings) {
        if (!(settings instanceof MongoDBSettings mongoDBSettings))
            return false;

        return this.settings.host.equals(mongoDBSettings.host)
            && this.settings.port.equals(mongoDBSettings.port)
            && this.settings.database.equals(mongoDBSettings.database)
            && this.settings.isWritable == mongoDBSettings.isWritable
            && this.settings.isQueryable == mongoDBSettings.isQueryable;
    }

    public void close() {
        if (mongoClient != null)
            mongoClient.close();

        if (noUserMongoClient != null)
            noUserMongoClient.close();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
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

        public String createSparkConnectionString() {
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
                .append("/");

            return builder.toString();
        }

        public String createNoUserConnectionString() {
            return "mongodb://" + host + ":" + port + "/" + ADMIN_NAME;
        }

    }

}
