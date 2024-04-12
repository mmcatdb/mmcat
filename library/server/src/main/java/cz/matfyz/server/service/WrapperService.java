package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.database.DatabaseEntity;
import cz.matfyz.server.entity.datainput.DataInputEntity;
import cz.matfyz.server.exception.DatabaseException;
import cz.matfyz.server.exception.DataInputException;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.matfyz.wrappermongodb.MongoDBProvider;
import cz.matfyz.wrappermongodb.MongoDBSettings;
import cz.matfyz.wrapperneo4j.Neo4jControlWrapper;
import cz.matfyz.wrapperneo4j.Neo4jProvider;
import cz.matfyz.wrapperneo4j.Neo4jSettings;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;
import cz.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;
import cz.matfyz.wrapperpostgresql.PostgreSQLSettings;

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

    public AbstractControlWrapper getControlWrapper(DatabaseEntity database) {
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

    private MongoDBControlWrapper getMongoDBControlWrapper(DatabaseEntity database) throws IllegalArgumentException, JsonProcessingException {
        if (!mongoDBCache.containsKey(database.id))
            mongoDBCache.put(database.id, createMongoDBProvider(database));

        final var provider = mongoDBCache.get(database.id);
        return new MongoDBControlWrapper(provider);
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    private static MongoDBProvider createMongoDBProvider(DatabaseEntity database) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(database.settings, MongoDBSettings.class);

        return new MongoDBProvider(settings);
    }

    // PostgreSQL

    private Map<Id, PostgreSQLProvider> postgreSQLCache = new TreeMap<>();

    private PostgreSQLControlWrapper getPostgreSQLControlWrapper(DatabaseEntity database) throws IllegalArgumentException, JsonProcessingException {
        if (!postgreSQLCache.containsKey(database.id))
            postgreSQLCache.put(database.id, createPostgreSQLProvider(database));

        final var provider = postgreSQLCache.get(database.id);
        return new PostgreSQLControlWrapper(provider);
    }

    private static PostgreSQLProvider createPostgreSQLProvider(DatabaseEntity database) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(database.settings, PostgreSQLSettings.class);

        return new PostgreSQLProvider(settings);
    }

    // Neo4j

    private Map<Id, Neo4jProvider> neo4jCache = new TreeMap<>();

    private Neo4jControlWrapper getNeo4jControlWrapper(DatabaseEntity database) throws IllegalArgumentException, JsonProcessingException {
        if (!neo4jCache.containsKey(database.id))
            neo4jCache.put(database.id, createNeo4jProvider(database));

        final var provider = neo4jCache.get(database.id);
        return new Neo4jControlWrapper(provider);
    }

    private static Neo4jProvider createNeo4jProvider(DatabaseEntity database) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(database.settings, Neo4jSettings.class);

        return new Neo4jProvider(settings);
    }
    
    
    
    // Added for DataInpu
    public AbstractControlWrapper getControlWrapper(DataInputEntity dataInput) {
        try {
            return switch (dataInput.type) {
                case mongodb -> getMongoDBControlWrapper(dataInput);
                case postgresql -> getPostgreSQLControlWrapper(dataInput);
                case neo4j -> getNeo4jControlWrapper(dataInput);
                default -> throw DataInputException.wrapperNotFound(dataInput);
            };
        }
        catch (Exception exception) {
            throw DataInputException.wrapperNotCreated(dataInput, exception);
        }
    }

    private MongoDBControlWrapper getMongoDBControlWrapper(DataInputEntity dataInput) throws IllegalArgumentException, JsonProcessingException {
        if (!mongoDBCache.containsKey(dataInput.id))
            mongoDBCache.put(dataInput.id, createMongoDBProvider(dataInput));

        final var provider = mongoDBCache.get(dataInput.id);
        return new MongoDBControlWrapper(provider);
    }

    private static MongoDBProvider createMongoDBProvider(DataInputEntity dataInput) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(dataInput.settings, MongoDBSettings.class);

        return new MongoDBProvider(settings);
    }

    // PostgreSQL

    private PostgreSQLControlWrapper getPostgreSQLControlWrapper(DataInputEntity dataInput) throws IllegalArgumentException, JsonProcessingException {
        if (!postgreSQLCache.containsKey(dataInput.id))
            postgreSQLCache.put(dataInput.id, createPostgreSQLProvider(dataInput));

        final var provider = postgreSQLCache.get(dataInput.id);
        return new PostgreSQLControlWrapper(provider);
    }

    private static PostgreSQLProvider createPostgreSQLProvider(DataInputEntity dataInput) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(dataInput.settings, PostgreSQLSettings.class);

        return new PostgreSQLProvider(settings);
    }

    // Neo4j

    private Neo4jControlWrapper getNeo4jControlWrapper(DataInputEntity dataInput) throws IllegalArgumentException, JsonProcessingException {
        if (!neo4jCache.containsKey(dataInput.id))
            neo4jCache.put(dataInput.id, createNeo4jProvider(dataInput));

        final var provider = neo4jCache.get(dataInput.id);
        return new Neo4jControlWrapper(provider);
    }

    private static Neo4jProvider createNeo4jProvider(DataInputEntity dataInput) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(dataInput.settings, Neo4jSettings.class);

        return new Neo4jProvider(settings);
    }


}
