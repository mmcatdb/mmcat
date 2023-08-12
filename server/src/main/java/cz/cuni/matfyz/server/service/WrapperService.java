package cz.cuni.matfyz.server.service;

import cz.cuni.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.exception.DatabaseException;
import cz.cuni.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.cuni.matfyz.wrappermongodb.MongoDBProvider;
import cz.cuni.matfyz.wrappermongodb.MongoDBSettings;
import cz.cuni.matfyz.wrapperneo4j.Neo4jControlWrapper;
import cz.cuni.matfyz.wrapperneo4j.Neo4jProvider;
import cz.cuni.matfyz.wrapperneo4j.Neo4jSettings;
import cz.cuni.matfyz.wrapperpostgresql.PostgreSQLProvider;
import cz.cuni.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;
import cz.cuni.matfyz.wrapperpostgresql.PostgreSQLSettings;

import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

/**
 * @author jachym.bartik
 */
@Service
public class WrapperService {

    public AbstractControlWrapper getControlWrapper(Database database) {
        try {
            return switch (database.type) {
                case mongodb -> getMongoDBControlWrapper(database);
                case postgresql -> getPostgreSQLControlWrapper(database);
                case neo4j -> getNeo4jControlWrapper(database);
                default -> throw DatabaseException.wrapperNotFound(database);
            };
        }
        catch (Exception exception) {
            throw DatabaseException.wrapperNotCreated(database, exception);
        }
    }

    // MongoDB

    private Map<Id, MongoDBProvider> mongoDBCache = new TreeMap<>();

    private MongoDBControlWrapper getMongoDBControlWrapper(Database database) throws IllegalArgumentException, JsonProcessingException {
        if (!mongoDBCache.containsKey(database.id))
            mongoDBCache.put(database.id, createMongoDBProvider(database));

        final var provider = mongoDBCache.get(database.id);
        return new MongoDBControlWrapper(provider);
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    private static MongoDBProvider createMongoDBProvider(Database database) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(database.settings, MongoDBSettings.class);

        return new MongoDBProvider(settings);
    }

    // PostgreSQL

    private Map<Id, PostgreSQLProvider> postgreSQLCache = new TreeMap<>();

    private PostgreSQLControlWrapper getPostgreSQLControlWrapper(Database database) throws IllegalArgumentException, JsonProcessingException {
        if (!postgreSQLCache.containsKey(database.id))
            postgreSQLCache.put(database.id, createPostgreSQLProvider(database));

        final var provider = postgreSQLCache.get(database.id);
        return new PostgreSQLControlWrapper(provider);
    }

    private static PostgreSQLProvider createPostgreSQLProvider(Database database) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(database.settings, PostgreSQLSettings.class);

        return new PostgreSQLProvider(settings);
    }

    // Neo4j

    private Map<Id, Neo4jProvider> neo4jCache = new TreeMap<>();

    private Neo4jControlWrapper getNeo4jControlWrapper(Database database) throws IllegalArgumentException, JsonProcessingException {
        if (!neo4jCache.containsKey(database.id))
            neo4jCache.put(database.id, createNeo4jProvider(database));

        final var provider = neo4jCache.get(database.id);
        return new Neo4jControlWrapper(provider);
    }

    private static Neo4jProvider createNeo4jProvider(Database database) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(database.settings, Neo4jSettings.class);

        return new Neo4jProvider(settings);
    }

}
