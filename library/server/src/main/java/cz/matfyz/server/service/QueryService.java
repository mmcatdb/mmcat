package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.datasource.Datasource;
import cz.matfyz.abstractwrappers.datasource.Kind;
import cz.matfyz.core.querying.queryresult.ResultList;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.algorithms.QueryToInstance;
import cz.matfyz.server.controller.QueryController.QueryPartDescription;
import cz.matfyz.server.controller.DatasourceController;
import cz.matfyz.server.controller.QueryController.QueryDescription;
import cz.matfyz.server.controller.QueryController.QueryInit;
import cz.matfyz.server.controller.QueryController.QueryVersionUpdate;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.entity.query.Query;
import cz.matfyz.server.entity.query.QueryVersion;
import cz.matfyz.server.repository.QueryRepository;
import cz.matfyz.server.repository.QueryRepository.QueryWithVersion;
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
    private LogicalModelService logicalModelService;

    @Autowired
    private WrapperService wrapperService;

    @Autowired
    private MappingService mappingService;

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

        final var kinds = logicalModelService
            .findAll(categoryId).stream()
            .flatMap(model -> {
                final DatasourceWrapper datasourceWrapper = model.datasource();
                final var control = wrapperService.getControlWrapper(datasourceWrapper);
                if (!control.isQueryable())
                    return List.<Kind>of().stream();

                datasources.put(datasourceWrapper.id(), datasourceWrapper);

                final var builder = new Datasource.Builder();
                mappingService.findAll(model.logicalModel().id()).forEach(mappingWrapper -> {
                    final var mapping = mappingWrapper.toMapping(category);
                    builder.mapping(mapping);
                });
                final var datasource = builder.build(datasourceWrapper.type, control, datasourceWrapper.id().toString());
                return datasource.kinds.stream();
            }).toList();

        return new KindsAndDatasources(kinds, datasources);
    }

    public QueryWithVersion createQuery(QueryInit init) {
        final var categoryInfo = categoryRepository.findInfo(init.categoryId());

        final var query = Query.createNew(init.categoryId(), init.label());
        final var version = QueryVersion.createNew(query.id(), categoryInfo.version, init.content(), List.of());

        repository.save(query);
        repository.save(version);

        return new QueryWithVersion(query, version);
    }

    public QueryVersion createQueryVersion(Id queryId, QueryVersionUpdate update) {
        final var version = QueryVersion.createNew(queryId, update.version(), update.content(), update.errors());
        version.version = update.version();
        version.content = update.content();
        version.errors = update.errors();

        repository.save(version);

        return version;
    }

    public QueryVersion updateQueryVersion(Id versionId, QueryVersionUpdate update) {
        final var version = repository.findVersion(versionId);
        version.version = update.version();
        version.content = update.content();
        version.errors = update.errors();

        repository.save(version);

        return version;
    }

    public void deleteQueryWithVersions(Id id) {
        repository.deleteQueryVersionsByQuery(id);
        repository.deleteQuery(id);
    }

}
