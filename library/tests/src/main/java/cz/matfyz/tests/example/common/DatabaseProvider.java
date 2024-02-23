package cz.matfyz.tests.example.common;

import cz.matfyz.abstractwrappers.database.Database.DatabaseType;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.Config;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.matfyz.wrappermongodb.MongoDBProvider;
import cz.matfyz.wrappermongodb.MongoDBSettings;
import cz.matfyz.wrapperneo4j.Neo4jControlWrapper;
import cz.matfyz.wrapperneo4j.Neo4jProvider;
import cz.matfyz.wrapperneo4j.Neo4jSettings;
import cz.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;
import cz.matfyz.wrapperpostgresql.PostgreSQLSettings;

public class DatabaseProvider {

    private final Config config;

    public DatabaseProvider(String namespace) {
        this.config = new Config(namespace);
    }

    // PostgreSQL

    private PostgreSQLProvider postgreSQLProvider;

    public PostgreSQLProvider getPostgreSQLProvider() {
        if (postgreSQLProvider == null) {
            postgreSQLProvider = new PostgreSQLProvider(new PostgreSQLSettings(
                config.getBool("isInDocker") ? "mmcat-postgresql" : "localhost",
                config.getBool("isInDocker") ? "5432" : "3204",
                config.get("database"),
                config.get("username"),
                config.get("password")
            ));
        }

        return postgreSQLProvider;
    }

    public TestDatabase<PostgreSQLControlWrapper> createPostgreSQL(SchemaCategory schema, String setupFileName) {
        final var wrapper = new PostgreSQLControlWrapper(getPostgreSQLProvider());
        return new TestDatabase<>(DatabaseType.postgresql, wrapper, schema, setupFileName);
    }

    // MongoDB

    private MongoDBProvider mongoDBProvider;

    public MongoDBProvider getMongoDBProvider() {
        if (mongoDBProvider == null) {
            mongoDBProvider = new MongoDBProvider(new MongoDBSettings(
                config.getBool("isInDocker") ? "mmcat-mongodb" : "localhost",
                config.getBool("isInDocker") ? "27017" : "3205",
                "admin",
                config.get("database"),
                config.get("username"),
                config.get("password")
            ));
        }

        return mongoDBProvider;
    }

    public TestDatabase<MongoDBControlWrapper> createMongoDB(SchemaCategory schema, String setupFileName) {
        final var wrapper = new MongoDBControlWrapper(getMongoDBProvider());
        return new TestDatabase<>(DatabaseType.mongodb, wrapper, schema, setupFileName);
    }

    // Neo4j

    private Neo4jProvider neo4jProvider;

    public Neo4jProvider getNeo4jProvider() {
        if (neo4jProvider == null) {
            neo4jProvider = new Neo4jProvider(new Neo4jSettings(
                config.getBool("isInDocker") ? "mmcat-neo4j" : "localhost",
                config.getBool("isInDocker") ? "7687" : "3206",
                "neo4j",
                "neo4j",
                config.get("password")
            ));
        }

        return neo4jProvider;
    }

    public TestDatabase<Neo4jControlWrapper> createNeo4j(SchemaCategory schema, String setupFileName) {
        final var wrapper = new Neo4jControlWrapper(getNeo4jProvider());
        return new TestDatabase<>(DatabaseType.neo4j, wrapper, schema, setupFileName);
    }

}
