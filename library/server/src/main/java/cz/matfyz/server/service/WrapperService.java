package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractDatasourceProvider;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.exception.DatasourceException;
import cz.matfyz.wrapperjsonld.JsonLdControlWrapper;
import cz.matfyz.wrapperjsonld.JsonLdProvider;
import cz.matfyz.wrapperjsonld.JsonLdProvider.JsonLdSettings;
import cz.matfyz.wrapperjson.JsonControlWrapper;
import cz.matfyz.wrapperjson.JsonProvider;
import cz.matfyz.wrapperjson.JsonProvider.JsonSettings;
import cz.matfyz.wrappercsv.CsvControlWrapper;
import cz.matfyz.wrappercsv.CsvProvider;
import cz.matfyz.wrappercsv.CsvProvider.CsvSettings;
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

@Service
public class WrapperService {

    public AbstractControlWrapper getControlWrapper(DatasourceWrapper datasource) {
        try {
            return switch (datasource.type) {
                case mongodb -> new MongoDBControlWrapper(getProvider(
                    datasource,
                    MongoDBSettings.class,
                    MongoDBProvider::new
                ));
                case postgresql -> new PostgreSQLControlWrapper(getProvider(
                    datasource,
                    PostgreSQLSettings.class,
                    PostgreSQLProvider::new
                ));
                case neo4j -> new Neo4jControlWrapper(getProvider(
                    datasource,
                    Neo4jSettings.class,
                    Neo4jProvider::new
                ));
                case jsonld -> getJsonLdControlWrapper(datasource);
                case json -> getJsonControlWrapper(datasource);
                case csv -> getCsvControlWrapper(datasource);
                default -> throw DatasourceException.wrapperNotFound(datasource);
            };
        }
        catch (Exception exception) {
            throw DatasourceException.wrapperNotCreated(datasource, exception);
        }
    }

    private Map<Id, AbstractDatasourceProvider> cachedProviders = new TreeMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    private interface CreateProviderFunction<TProvider extends AbstractDatasourceProvider, TSettings> {
        TProvider apply(TSettings settings) throws JsonProcessingException;
    }

    @SuppressWarnings("unchecked")
    private <TProvider extends AbstractDatasourceProvider, TSettings> TProvider getProvider(
        DatasourceWrapper datasource,
        Class<TSettings> clazz,
        CreateProviderFunction<TProvider, TSettings> create
    ) throws JsonProcessingException {
        final TSettings settings = mapper.treeToValue(datasource.settings, clazz);

        // First, we try to get the provider from the cache. If it's there and still valid, we return it.
        final TProvider cached = (TProvider) cachedProviders.get(datasource.id);
        if (cached != null && cached.isStillValid(settings))
            return cached;

        // If the provider is invalid, we should close it.
        if (cached != null) {
            System.out.println("Reseting the provider for \"" + datasource.label + "\"");
            cached.close();
        }
        else {
            System.out.println("Creating new provider for \"" + datasource.label + "\"");
        }

        // If the provider is not in the cache or is not valid anymore, we create a new one and put it into the cache.
        final TProvider provider = create.apply(settings);
        cachedProviders.put(datasource.id, provider);

        return provider;
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

    // Json

    private JsonControlWrapper getJsonControlWrapper(DatasourceWrapper datasource) throws IllegalArgumentException, JsonProcessingException {
        final var provider = createJsonProvider(datasource);
        return new JsonControlWrapper(provider);
    }

    private static JsonProvider createJsonProvider(DatasourceWrapper datasource) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(datasource.settings, JsonSettings.class);

        return new JsonProvider(settings);
    }

    // Csv

    private CsvControlWrapper getCsvControlWrapper(DatasourceWrapper datasource) throws IllegalArgumentException, JsonProcessingException {
        final var provider = createCsvProvider(datasource);
        return new CsvControlWrapper(provider);
    }

    private static CsvProvider createCsvProvider(DatasourceWrapper datasource) throws IllegalArgumentException, JsonProcessingException {
        final var settings = mapper.treeToValue(datasource.settings, CsvSettings.class);

        return new CsvProvider(settings);
    }

}
