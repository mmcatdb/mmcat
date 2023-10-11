package cz.matfyz.server.setup;

import cz.matfyz.abstractwrappers.database.Database.DatabaseType;
import cz.matfyz.server.configuration.SetupProperties;
import cz.matfyz.server.entity.database.Database;
import cz.matfyz.server.entity.database.DatabaseInit;
import cz.matfyz.server.service.DatabaseService;
import cz.matfyz.wrappermongodb.MongoDBSettings;
import cz.matfyz.wrapperneo4j.Neo4jSettings;
import cz.matfyz.wrapperpostgresql.PostgreSQLSettings;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSetup {
    
    @Autowired
    private SetupProperties properties;

    @Autowired
    DatabaseService databaseService;

    private ObjectMapper mapper = new ObjectMapper();
    
    public List<Database> createDatabases() {
        final List<DatabaseInit> inits = new ArrayList<>();

        inits.add(createPostgreSQL());
        inits.add(createMongoDB());
        inits.add(createNeo4j());
        
        return inits.stream().map(init -> databaseService.createNew(init)).toList();
    }

    private DatabaseInit createPostgreSQL() {
        final var settings = new PostgreSQLSettings(
            properties.isInDocker() ? "mmcat-postgresql" : "localhost",
            properties.postgresqlPort(),
            properties.database(),
            properties.username(),
            properties.password()
        );

        return new DatabaseInit("PostgreSQL - example", mapper.valueToTree(settings), DatabaseType.postgresql);
    }

    private DatabaseInit createMongoDB() {
        final var settings = new MongoDBSettings(
            properties.isInDocker() ? "mmcat-mongodb" : "localhost",
            properties.mongodbPort(),
            "admin",
            properties.database(),
            properties.username(),
            properties.password()
        );

        return new DatabaseInit("MongoDB - example", mapper.valueToTree(settings), DatabaseType.mongodb);
    }

    private DatabaseInit createNeo4j() {
        final var settings = new Neo4jSettings(
            properties.isInDocker() ? "mmcat-neo4j" : "localhost",
            properties.neo4jPort(),
            "neo4j",
            "neo4j",
            properties.password()
        );

        return new DatabaseInit("Neo4j - example", mapper.valueToTree(settings), DatabaseType.neo4j);
    }

}
