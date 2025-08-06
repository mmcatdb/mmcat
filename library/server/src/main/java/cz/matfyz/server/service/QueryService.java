package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.BaseControlWrapper.ControlWrapperProvider;
import cz.matfyz.abstractwrappers.BaseControlWrapper.DefaultControlWrapperProvider;
import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.ListResult;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.querying.QueryEvolutionResult.QueryEvolutionError;
import cz.matfyz.querying.QueryToInstance;
import cz.matfyz.querying.core.QueryDescription;
import cz.matfyz.server.controller.QueryController.QueryInit;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.Query;
import cz.matfyz.server.entity.SchemaCategoryEntity;
import cz.matfyz.server.entity.datasource.DatasourceEntity;
import cz.matfyz.server.entity.evolution.QueryEvolution;
import cz.matfyz.server.repository.DatasourceRepository;
import cz.matfyz.server.repository.EvolutionRepository;
import cz.matfyz.server.repository.MappingRepository;
import cz.matfyz.server.repository.QueryRepository;
import cz.matfyz.server.repository.SchemaCategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QueryService {

    @Autowired
    private QueryRepository repository;

    @Autowired
    private EvolutionRepository evolutionRepository;

    @Autowired
    private DatasourceRepository datasourceRepository;

    @Autowired
    private WrapperService wrapperService;

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private SchemaCategoryRepository categoryRepository;

    public ListResult executeQuery(Id categoryId, String queryString) {
        final var categoryEntity = categoryRepository.find(categoryId);
        final var category = categoryEntity.toSchemaCategory();
        final var datasources = getDatasources(categoryEntity.id(), category);

        return new QueryToInstance(datasources.provider, category, queryString, datasources.kinds, null).execute();
    }

    public QueryDescription describeQuery(Id categoryId, String queryString) {
        final var categoryEntity = categoryRepository.find(categoryId);
        final var category = categoryEntity.toSchemaCategory();
        final var datasources = getDatasources(categoryEntity.id(), category);

        return new QueryToInstance(datasources.provider, category, queryString, datasources.kinds, null).describe();
    }

    private record KindsAndDatasources(
        List<Mapping> kinds,
        Map<Id, DatasourceEntity> datasourceEntities,
        ControlWrapperProvider provider
    ) {}

    private KindsAndDatasources getDatasources(Id categoryId, SchemaCategory category) {
        final var provider = new DefaultControlWrapperProvider();

        final Map<Id, Datasource> datasources = new TreeMap<>();
        final Map<Id, DatasourceEntity> datasourceEntities = new TreeMap<>();
        datasourceRepository
            .findAllInCategory(categoryId)
            .forEach(entity -> {
                final var control = wrapperService.getControlWrapper(entity);
                if (!control.isQueryable())
                    return;

                final var datasource = entity.toDatasource();
                datasources.put(entity.id(), datasource);
                datasourceEntities.put(entity.id(), entity);
                provider.setControlWrapper(datasource, control);
            });


        final List<Mapping> kinds = new ArrayList<>();
        mappingRepository
            .findAllInCategory(categoryId)
            .forEach(entity -> {
                final var datasource = datasources.get(entity.datasourceId);
                if (datasource == null)
                    // The datasource isn't queryable so it was discarded in the previous step.
                    return;

                kinds.add(entity.toMapping(datasource, category));
            });

       return new KindsAndDatasources(kinds, datasourceEntities, provider);
    }

    public Query create(QueryInit init) {
        final var category = categoryRepository.find(init.categoryId());

        final var newVersion = category.systemVersion().generateNext();
        final var query = Query.createNew(newVersion, init.categoryId(), init.label(), init.content());
        final var evolution = QueryEvolution.createNew(category.id(), newVersion, query.id(), init.content(), "", List.of());

        repository.save(query);
        evolutionRepository.create(evolution);

        propagateEvolution(category, evolution);

        return query;
    }

    public void update(Query query, String content, List<QueryEvolutionError> errors) {
        final var category = categoryRepository.find(query.categoryId);

        final var newVersion = category.systemVersion().generateNext();
        final var evolution = QueryEvolution.createNew(category.id(), newVersion, query.id(), content, query.content, errors);

        query.updateVersion(newVersion, category.systemVersion());
        query.content = content;
        query.errors = errors;

        repository.save(query);
        evolutionRepository.create(evolution);

        propagateEvolution(category, evolution);
    }

    private void propagateEvolution(SchemaCategoryEntity category, QueryEvolution evolution) {
        final var oldVersion = category.systemVersion;

        category.systemVersion = evolution.version;
        category.updateLastValid(evolution.version);
        categoryRepository.save(category);

        // All other queries are independed on this query so we can propagate the evolution.
        // TODO make more efficient with orm.
        repository.findAllInCategory(category.id(), null).stream()
            .filter(query -> query.lastValid().equals(oldVersion))
            .forEach(query -> {
                query.updateLastValid(evolution.version);
                repository.save(query);
            });

        // The same holds true for mappings.
        mappingRepository.findAllInCategory(category.id()).stream()
            .filter(mapping -> mapping.lastValid().equals(oldVersion))
            .forEach(mapping -> {
                mapping.updateLastValid(evolution.version);
                mappingRepository.save(mapping);
            });

    }

    // TODO Allow only soft-delete because of the evolution.
    public void delete(Id id) {
        evolutionRepository.deleteQueryEvolutions(id);
        repository.delete(id);
    }

}
