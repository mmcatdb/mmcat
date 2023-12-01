package cz.matfyz.server.service;

import cz.matfyz.abstractwrappers.database.Database;
import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.abstractwrappers.queryresult.ResultList;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.algorithms.QueryToInstance;
import cz.matfyz.server.builder.MappingBuilder;
import cz.matfyz.server.controller.QueryController.QueryPartDescription;
import cz.matfyz.server.controller.QueryController.QueryDescription;
import cz.matfyz.server.controller.QueryController.QueryInit;
import cz.matfyz.server.controller.QueryController.QueryVersionUpdate;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.database.DatabaseEntity;
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

/**
 * @author jachym.bartik
 */
@RestController
public class QueryService {

    @Autowired
    private QueryRepository repository;

    @Autowired
    private LogicalModelService logicalModelService;

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private WrapperService wrapperService;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private SchemaCategoryRepository categoryRepository;

    public ResultList executeQuery(Id categoryId, String queryString) {
        final var categoryWrapper = categoryRepository.find(categoryId);
        final var category = categoryWrapper.toSchemaCategory();
        final var kindsAndDatabases = defineKinds(categoryWrapper.id, category);

        return new QueryToInstance(category, queryString, kindsAndDatabases.kinds).execute();
    }

    public QueryDescription describeQuery(Id categoryId, String queryString) {
        final var categoryWrapper = categoryRepository.find(categoryId);
        final var category = categoryWrapper.toSchemaCategory();
        final var kindsAndDatabases = defineKinds(categoryWrapper.id, category);

        final var rawDescriptions = new QueryToInstance(category, queryString, kindsAndDatabases.kinds).describe();

        final var parts = rawDescriptions.parts().stream().map(d -> {
            final var database = kindsAndDatabases.databases.get(new Id(d.databaseIdentifier()));
            return new QueryPartDescription(database.toInfo(), d.query());
        }).toList();

        return new QueryDescription(parts);
    }

    private static record KindsAndDatabases(
        List<Kind> kinds,
        Map<Id, DatabaseEntity> databases
    ) {}

    private KindsAndDatabases defineKinds(Id categoryId, SchemaCategory category) {
        final Map<Id, DatabaseEntity> databases = new TreeMap<>();

        final var kinds = logicalModelService
            .findAll(categoryId).stream()
            .flatMap(logicalModel -> {
                final var databaseEntity = databaseService.find(logicalModel.databaseId);
                databases.put(databaseEntity.id, databaseEntity);

                final var builder = new Database.Builder();
                mappingService.findAll(logicalModel.id).forEach(mappingWrapper -> {
                    final var mapping = MappingBuilder.build(category, mappingWrapper);
                    builder.mapping(mapping);
                });
                final var database = builder.build(databaseEntity.type, wrapperService.getControlWrapper(databaseEntity), databaseEntity.id.toString());
                return database.kinds.stream();
            }).toList();

        return new KindsAndDatabases(kinds, databases);
    }

    public QueryWithVersion createQuery(QueryInit init) {
        final var categoryInfo = categoryRepository.findInfo(init.categoryId());

        final var query = Query.createNew(init.categoryId(), init.label());
        final var version = QueryVersion.createNew(query.id, categoryInfo.version, init.content(), List.of());

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

    public boolean deleteQueryWithVersions(Id id) {
        return
            repository.deleteQueryVersionsByQuery(id) &&
            repository.deleteQuery(id);
    }

}
