package cz.matfyz.server.example.common;

import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.server.entity.datasource.DatasourceInit;
import cz.matfyz.server.global.Configuration.SetupProperties;
import cz.matfyz.wrappermongodb.MongoDBProvider.MongoDBSettings;
import cz.matfyz.wrapperneo4j.Neo4jProvider.Neo4jSettings;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider.PostgreSQLSettings;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DatasourceSettings {

    private SetupProperties properties;
    private String database;

    public DatasourceSettings(SetupProperties properties, String database) {
        this.properties = properties;
        this.database = database;
    }

    private ObjectMapper mapper = new ObjectMapper();

    public DatasourceInit createPostgreSQL(String label) {
        final var settings = new PostgreSQLSettings(
            properties.isInDocker() ? "mmcat-postgresql" : "localhost",
            properties.isInDocker() ? "5432" : "3204",
            database,
            properties.username(),
            properties.password(),
            true,
            true,
            false
        );

        return new DatasourceInit(label, DatasourceType.postgresql, mapper.valueToTree(settings));
    }

    public DatasourceInit createMongoDB(String label) {
        final var settings = new MongoDBSettings(
            properties.isInDocker() ? "mmcat-mongodb" : "localhost",
            properties.isInDocker() ? "27017" : "3205",
            "admin",
            database,
            properties.username(),
            properties.password(),
            true,
            true,
            false
        );

        return new DatasourceInit(label, DatasourceType.mongodb, mapper.valueToTree(settings));
    }

    public DatasourceInit createNeo4j(String label) {
        final var settings = new Neo4jSettings(
            properties.isInDocker() ? "mmcat-neo4j" : "localhost",
            properties.isInDocker() ? "7687" : "3206",
            "neo4j",
            "neo4j",
            properties.password(),
            true,
            // TODO enable when it's implemented
            false,
            false
        );

        return new DatasourceInit(label, DatasourceType.neo4j, mapper.valueToTree(settings));
    }

}
