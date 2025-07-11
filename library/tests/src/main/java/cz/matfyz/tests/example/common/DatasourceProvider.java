package cz.matfyz.tests.example.common;

import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.Config;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.matfyz.wrappermongodb.MongoDBProvider;
import cz.matfyz.wrappermongodb.MongoDBProvider.MongoDBSettings;
import cz.matfyz.wrapperneo4j.Neo4jControlWrapper;
import cz.matfyz.wrapperneo4j.Neo4jProvider;
import cz.matfyz.wrapperneo4j.Neo4jProvider.Neo4jSettings;
import cz.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider.PostgreSQLSettings;

import org.checkerframework.checker.nullness.qual.Nullable;

public class DatasourceProvider {

    private final Config config;
    /** If null, the value is loaded from the config. */
    private final @Nullable String database;

    public DatasourceProvider(String namespace, String database) {
        this.config = new Config(namespace);
        this.database = database;
    }

    public DatasourceProvider(String namespace) {
        this(namespace, null);
    }

    // PostgreSQL

    private PostgreSQLProvider postgreSQLProvider;

    public PostgreSQLProvider getPostgreSQLProvider() {
        if (postgreSQLProvider == null) {
            postgreSQLProvider = new PostgreSQLProvider(new PostgreSQLSettings(
                config.getBool("isInDocker") ? "mmcat-postgresql" : "localhost",
                config.getBool("isInDocker") ? "5432" : "3204",
                database != null ? database : config.get("database"),
                config.get("username"),
                config.get("password"),
                true,
                true,
                false
            ));
        }

        return postgreSQLProvider;
    }

    public TestDatasource<PostgreSQLControlWrapper> createPostgreSQL(String identifier, SchemaCategory schema, String setupFileName) {
        final var wrapper = new PostgreSQLControlWrapper(getPostgreSQLProvider(), identifier);
        return new TestDatasource<>(DatasourceType.postgresql, identifier, wrapper, schema, setupFileName);
    }

    // MongoDB

    private MongoDBProvider mongoDBProvider;

    public MongoDBProvider getMongoDBProvider() {
        if (mongoDBProvider == null) {
            mongoDBProvider = new MongoDBProvider(new MongoDBSettings(
                config.getBool("isInDocker") ? "mmcat-mongodb" : "localhost",
                config.getBool("isInDocker") ? "27017" : "3205",
                "admin",
                database != null ? database : config.get("database"),
                config.get("username"),
                config.get("password"),
                true,
                true,
                false
            ));
        }

        return mongoDBProvider;
    }

    public TestDatasource<MongoDBControlWrapper> createMongoDB(String identifier, SchemaCategory schema, String setupFileName) {
        final var wrapper = new MongoDBControlWrapper(getMongoDBProvider(), identifier);
        return new TestDatasource<>(DatasourceType.mongodb, identifier, wrapper, schema, setupFileName);
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
                config.get("password"),
                true,
                true,
                false
            ));
        }

        return neo4jProvider;
    }

    public TestDatasource<Neo4jControlWrapper> createNeo4j(String identifier, SchemaCategory schema, String setupFileName) {
        final var wrapper = new Neo4jControlWrapper(getNeo4jProvider(), identifier);
        return new TestDatasource<>(DatasourceType.neo4j, identifier, wrapper, schema, setupFileName);
    }

}
