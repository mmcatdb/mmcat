package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.exception.DatasourceException;
import cz.matfyz.wrapperjsonld.JsonLdControlWrapper;
import cz.matfyz.wrapperjsonld.JsonLdProvider;
import cz.matfyz.wrapperjsonld.JsonLdProvider.JsonLdSettings;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.matfyz.wrappermongodb.MongoDBProvider;
import cz.matfyz.wrappermongodb.MongoDBProvider.MongoDBSettings;
import cz.matfyz.wrapperneo4j.Neo4jControlWrapper;
import cz.matfyz.wrapperneo4j.Neo4jProvider;
import cz.matfyz.wrapperneo4j.Neo4jProvider.Neo4jSettings;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider.PostgreSQLSettings;
import cz.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;

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

    public AbstractControlWrapper getControlWrapper(DatasourceWrapper datasource) {
        try {
            return switch (datasource.type) {
                case mongodb -> getMongoDBControlWrapper(datasource);
                case postgresql -> getPostgreSQLControlWrapper(datasource);
                case neo4j -> getNeo4jControlWrapper(datasource);
                case jsonld -> getJsonLdControlWrapper(datasource);
                default -> throw DatasourceException.wrapperNotFound(datasource);
            };
        }
        catch (Exception exception) {
            throw DatasourceException.wrapperNotCreated(datasource, exception);
        }
    }

    // MongoDB

    private Map<Id, MongoDBProvider> mongoDBCache = new TreeMap<>();

    private MongoDBControlWrapper getMongoDBControlWrapper(DatasourceWrapper datasource) throws IllegalArgumentException, JsonProcessingException {
        if (!mongoDBCache.containsKey(datasource.id))
            mongoDBCache.put(datasource.id, createMongoDBProvider(datasource));

        final var provider = mongoDBCache.get(datasource.id);
        return new MongoDBControlWrapper(provider);
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    private static MongoDBProvider createMongoDBProvider(DatasourceWrapper datasource) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(datasource.settings, MongoDBSettings.class);

        return new MongoDBProvider(settings);
    }

    // PostgreSQL

    private Map<Id, PostgreSQLProvider> postgreSQLCache = new TreeMap<>();

    private PostgreSQLControlWrapper getPostgreSQLControlWrapper(DatasourceWrapper datasource) throws IllegalArgumentException, JsonProcessingException {
        if (!postgreSQLCache.containsKey(datasource.id))
            postgreSQLCache.put(datasource.id, createPostgreSQLProvider(datasource));

        final var provider = postgreSQLCache.get(datasource.id);
        return new PostgreSQLControlWrapper(provider);
    }

    private static PostgreSQLProvider createPostgreSQLProvider(DatasourceWrapper datasource) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(datasource.settings, PostgreSQLSettings.class);

        return new PostgreSQLProvider(settings);
    }

    // Neo4j

    private Map<Id, Neo4jProvider> neo4jCache = new TreeMap<>();

    private Neo4jControlWrapper getNeo4jControlWrapper(DatasourceWrapper datasource) throws IllegalArgumentException, JsonProcessingException {
        if (!neo4jCache.containsKey(datasource.id))
            neo4jCache.put(datasource.id, createNeo4jProvider(datasource));

        final var provider = neo4jCache.get(datasource.id);
        return new Neo4jControlWrapper(provider);
    }

    private static Neo4jProvider createNeo4jProvider(DatasourceWrapper datasource) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(datasource.settings, Neo4jSettings.class);

        return new Neo4jProvider(settings);
    }

    // JsonLd

    private JsonLdControlWrapper getJsonLdControlWrapper(DatasourceWrapper datasource) throws IllegalArgumentException, JsonProcessingException {
        final var provider = createJsonLdProvider(datasource);
        return new JsonLdControlWrapper(provider);
    }

    private static JsonLdProvider createJsonLdProvider(DatasourceWrapper datasource) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(datasource.settings, JsonLdSettings.class);

        return new JsonLdProvider(settings);
    }

}
