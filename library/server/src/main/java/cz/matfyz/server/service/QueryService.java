package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.datasource.Datasource;
import cz.matfyz.abstractwrappers.datasource.Kind;
import cz.matfyz.core.querying.queryresult.ResultList;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.querying.QueryEvolutionResult.QueryEvolutionError;
import cz.matfyz.querying.algorithms.QueryToInstance;
import cz.matfyz.server.controller.QueryController.QueryPartDescription;
import cz.matfyz.server.controller.DatasourceController;
import cz.matfyz.server.controller.QueryController.QueryDescription;
import cz.matfyz.server.controller.QueryController.QueryInit;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.Query;
import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.evolution.QueryEvolution;
import cz.matfyz.server.repository.EvolutionRepository;
import cz.matfyz.server.repository.LogicalModelRepository;
import cz.matfyz.server.repository.MappingRepository;
import cz.matfyz.server.repository.QueryRepository;
import cz.matfyz.server.repository.SchemaCategoryRepository;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueryService {

    @Autowired
    private QueryRepository repository;

    @Autowired
    private EvolutionRepository evolutionRepository;

    @Autowired
    private LogicalModelRepository logicalModelRepository;

    @Autowired
    private WrapperService wrapperService;

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private SchemaCategoryRepository categoryRepository;

    @Autowired
    private DatasourceController datasourceController;

    public ResultList executeQuery(Id categoryId, String queryString) {
        final var categoryWrapper = categoryRepository.find(categoryId);
        final var category = categoryWrapper.toSchemaCategory();
        final var kindsAndDatasources = defineKinds(categoryWrapper.id(), category);

        return new QueryToInstance(category, queryString, kindsAndDatasources.kinds).execute();
    }

    public QueryDescription describeQuery(Id categoryId, String queryString) {
        final var categoryWrapper = categoryRepository.find(categoryId);
        final var category = categoryWrapper.toSchemaCategory();
        final var kindsAndDatasources = defineKinds(categoryWrapper.id(), category);

        final var rawDescriptions = new QueryToInstance(category, queryString, kindsAndDatasources.kinds).describe();

        final var parts = rawDescriptions.parts().stream().map(d -> {
            final var datasource = kindsAndDatasources.datasources.get(new Id(d.datasourceIdentifier()));
            final var datasourceDetail = datasourceController.datasourceToDetail(datasource);
            return new QueryPartDescription(datasourceDetail, d.query());
        }).toList();

        return new QueryDescription(parts);
    }

    private record KindsAndDatasources(
        List<Kind> kinds,
        Map<Id, DatasourceWrapper> datasources
    ) {}

    private KindsAndDatasources defineKinds(Id categoryId, SchemaCategory category) {
        final Map<Id, DatasourceWrapper> datasources = new TreeMap<>();

        final var kinds = logicalModelRepository
            .findAllInCategory(categoryId).stream()
            .flatMap(model -> {
                final DatasourceWrapper datasourceWrapper = model.datasource();
                final var control = wrapperService.getControlWrapper(datasourceWrapper);
                if (!control.isQueryable())
                    return List.<Kind>of().stream();

                datasources.put(datasourceWrapper.id(), datasourceWrapper);

                final var builder = new Datasource.Builder();
                mappingRepository.findAll(model.logicalModel().id()).forEach(mappingWrapper -> {
                    final var mapping = mappingWrapper.toMapping(category);
                    builder.mapping(mapping);
                });
                final var datasource = builder.build(datasourceWrapper.type, control, datasourceWrapper.id().toString());
                return datasource.kinds.stream();
            }).toList();

        return new KindsAndDatasources(kinds, datasources);
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

    private void propagateEvolution(SchemaCategoryWrapper category, QueryEvolution evolution) {
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
        mappingRepository.findAll().stream()
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
