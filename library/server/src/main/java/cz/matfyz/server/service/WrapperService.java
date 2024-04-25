package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.database.DatabaseEntity;
import cz.matfyz.server.entity.datasource.DataSourceEntity;
import cz.matfyz.server.exception.DatabaseException;
import cz.matfyz.server.exception.DataSourceException;
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
    public AbstractControlWrapper getControlWrapper(DataSourceEntity dataSource) {
        try {
            return switch (dataSource.type) {
                case mongodb -> getMongoDBControlWrapper(dataSource);
                case postgresql -> getPostgreSQLControlWrapper(dataSource);
                case neo4j -> getNeo4jControlWrapper(dataSource);
                default -> throw DataSourceException.wrapperNotFound(dataSource);
            };
        }
        catch (Exception exception) {
            throw DataSourceException.wrapperNotCreated(dataSource, exception);
        }
    }

    private MongoDBControlWrapper getMongoDBControlWrapper(DataSourceEntity dataSource) throws IllegalArgumentException, JsonProcessingException {
        if (!mongoDBCache.containsKey(dataSource.id))
            mongoDBCache.put(dataSource.id, createMongoDBProvider(dataSource));

        final var provider = mongoDBCache.get(dataSource.id);
        return new MongoDBControlWrapper(provider);
    }

    private static MongoDBProvider createMongoDBProvider(DataSourceEntity dataSource) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(dataSource.settings, MongoDBSettings.class);

        return new MongoDBProvider(settings);
    }

    // PostgreSQL

    private PostgreSQLControlWrapper getPostgreSQLControlWrapper(DataSourceEntity dataSource) throws IllegalArgumentException, JsonProcessingException {
        if (!postgreSQLCache.containsKey(dataSource.id))
            postgreSQLCache.put(dataSource.id, createPostgreSQLProvider(dataSource));

        final var provider = postgreSQLCache.get(dataSource.id);
        return new PostgreSQLControlWrapper(provider);
    }

    private static PostgreSQLProvider createPostgreSQLProvider(DataSourceEntity dataSource) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(dataSource.settings, PostgreSQLSettings.class);

        return new PostgreSQLProvider(settings);
    }

    // Neo4j

    private Neo4jControlWrapper getNeo4jControlWrapper(DataSourceEntity dataSource) throws IllegalArgumentException, JsonProcessingException {
        if (!neo4jCache.containsKey(dataSource.id))
            neo4jCache.put(dataSource.id, createNeo4jProvider(dataSource));

        final var provider = neo4jCache.get(dataSource.id);
        return new Neo4jControlWrapper(provider);
    }

    private static Neo4jProvider createNeo4jProvider(DataSourceEntity dataSource) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(dataSource.settings, Neo4jSettings.class);

        return new Neo4jProvider(settings);
    }


}
