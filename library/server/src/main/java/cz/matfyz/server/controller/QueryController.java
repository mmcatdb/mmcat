package cz.matfyz.server.controller;

import cz.matfyz.evolution.querying.QueryEvolutionResult.QueryEvolutionError;
import cz.matfyz.querying.core.QueryDescription;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.Query;
import cz.matfyz.server.entity.QueryStats;
import cz.matfyz.server.entity.evolution.QueryEvolution;
import cz.matfyz.server.repository.EvolutionRepository;
import cz.matfyz.server.repository.QueryRepository;
import cz.matfyz.server.service.QueryService;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
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
        /** If defined, the execution should count towards the query's stats. */
        @Nullable Id queryId,
        String queryString
    ) {}

    public record QueryResult(
        List<String> rows,
        @Nullable QueryStats stats
    ) {}

    @PostMapping("/queries/execute")
    public QueryResult executeQuery(@RequestBody QueryInput data) {
        final var execution = service.executeQuery(data.categoryId, data.queryString);
        var stats = service.computeQueryStats(execution);

        if (data.queryId != null) {
            final var query = repository.find(data.queryId);
            stats = query.stats.merge(stats);
            query.stats = stats;
            repository.save(query);
        }

        return new QueryResult(execution.result().toJsonArray(), stats);
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
        List<QueryEvolutionError> errors,
        /**
         * If true, ve should forget the query history.
         * Basically the new version is too different for the old stats to make sense.
         */
        boolean isResetStats
    ) {}

    @PutMapping("/queries/{queryId}")
    public Query updateQuery(@PathVariable Id queryId, @RequestBody QueryEdit edit) {
        final var query = repository.find(queryId);
        service.update(query, edit.content, edit.errors, edit.isResetStats);

        return query;
    }

    @PutMapping("/queries/{queryId}/stats")
    public Query updateQueryStats(@PathVariable Id queryId, @RequestBody QueryStats stats) {
        final var query = repository.find(queryId);

        query.stats = stats;

        repository.save(query);

        return query;
    }

}
