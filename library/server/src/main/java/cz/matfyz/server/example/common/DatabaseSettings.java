package cz.matfyz.server.example.common;

import cz.matfyz.abstractwrappers.database.Database.DatabaseType;
import cz.matfyz.server.configuration.SetupProperties;
import cz.matfyz.server.entity.database.DatabaseInit;
import cz.matfyz.wrappermongodb.MongoDBSettings;
import cz.matfyz.wrapperneo4j.Neo4jSettings;
import cz.matfyz.wrapperpostgresql.PostgreSQLSettings;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DatabaseSettings {

    private SetupProperties properties;
    private String database;

    public DatabaseSettings(SetupProperties properties, String database) {
        this.properties = properties;
        this.database = database;
    }

    private ObjectMapper mapper = new ObjectMapper();

    public DatabaseInit createPostgreSQL(String label) {
        final var settings = new PostgreSQLSettings(
            properties.isInDocker() ? "mmcat-postgresql" : "localhost",
            properties.isInDocker() ? "5432" : "3204",
            database,
            properties.username(),
            properties.password()
        );

        return new DatabaseInit(label, mapper.valueToTree(settings), DatabaseType.postgresql);
    }

    public DatabaseInit createMongoDB(String label) {
        final var settings = new MongoDBSettings(
            properties.isInDocker() ? "mmcat-mongodb" : "localhost",
            properties.isInDocker() ? "27017" : "3205",
            "admin",
            database,
            properties.username(),
            properties.password()
        );

        return new DatabaseInit(label, mapper.valueToTree(settings), DatabaseType.mongodb);
    }

    public DatabaseInit createNeo4j(String label) {
        final var settings = new Neo4jSettings(
            properties.isInDocker() ? "mmcat-neo4j" : "localhost",
            properties.isInDocker() ? "7687" : "3206",
            "neo4j",
            "neo4j",
            properties.password()
        );

        return new DatabaseInit(label, mapper.valueToTree(settings), DatabaseType.neo4j);
    }

}
