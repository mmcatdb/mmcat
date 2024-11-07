package cz.matfyz.server.controller;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.evolution.querying.QueryEvolutionResult.QueryEvolutionError;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.Query;
import cz.matfyz.server.entity.datasource.DatasourceDetail;
import cz.matfyz.server.entity.evolution.QueryEvolution;
import cz.matfyz.server.repository.EvolutionRepository;
import cz.matfyz.server.repository.QueryRepository;
import cz.matfyz.server.service.QueryService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QueryController {

    @Autowired
    private QueryService service;

    @Autowired
    private QueryRepository repository;

    @Autowired
    private EvolutionRepository evolutionRepository;

    public record QueryInput(
        Id categoryId,
        String queryString
    ) {}

    public record QueryResult(
        List<String> rows
    ) {}

    @PostMapping("/queries/execute")
    public QueryResult executeQuery(@RequestBody QueryInput data) {
        final var result = service.executeQuery(data.categoryId, data.queryString);

        return new QueryResult(result.toJsonArray());
    }

    public record QueryDescription(
        List<QueryPartDescription> parts
    ) {}

    public record QueryPartDescription(
        DatasourceDetail datasource,
        String content,
        String structure
    ) {
        public QueryPartDescription(DatasourceDetail datasource, QueryStatement query) {
            this(datasource, query.content().toString(), query.structure().toString());
        }
    }

    @PostMapping("/queries/describe")
    public QueryDescription describeQuery(@RequestBody QueryInput data) {
        return service.describeQuery(data.categoryId, data.queryString);
    }

    @GetMapping("/schema-categories/{categoryId}/queries")
    public List<Query> getQueriesInCategory(@PathVariable Id categoryId) {
        return repository.findAllInCategory(categoryId, null);
    }

    @GetMapping("/queries/{queryId}")
    public Query getQuery(@PathVariable Id queryId) {
        return repository.find(queryId);
    }

    private record QueryWithVersions(
        Query query,
        List<QueryEvolution> versions
    ) {}

    /** @deprecated Currently not used, but might be ... Delete if necessary. */
    @GetMapping("/queries/{queryId}/with-versions")
    public QueryWithVersions getQueryWithVersions(@PathVariable Id queryId) {
        final var query = repository.find(queryId);
        final var versions = evolutionRepository.findAllQueryEvolutions(queryId);

        return new QueryWithVersions(query, versions);
    }

    public record QueryInit(
        Id categoryId,
        String label,
        String content
    ) {}

    @PostMapping("/queries")
    public Query createQuery(@RequestBody QueryInit init) {
        return service.create(init);
    }

    @DeleteMapping("/queries/{queryId}")
    public void deleteQuery(@PathVariable Id queryId) {
        service.delete(queryId);
    }

    private record QueryEdit(
        String content,
        List<QueryEvolutionError> errors
    ) {}

    @PutMapping("/queries/{queryId}")
    public Query updateQuery(@PathVariable Id queryId, @RequestBody QueryEdit update) {
        final var query = repository.find(queryId);
        service.update(query, update.content, update.errors);

        return query;
    }

}
