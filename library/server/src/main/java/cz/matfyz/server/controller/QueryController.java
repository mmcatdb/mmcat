package cz.matfyz.server.controller;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.query.Query;
import cz.matfyz.server.entity.query.QueryVersion;
import cz.matfyz.server.repository.QueryRepository;
import cz.matfyz.server.repository.QueryRepository.QueryWithVersion;
import cz.matfyz.server.service.QueryService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author jachym.bartik
 */
@RestController
public class QueryController {

    @Autowired
    private QueryService service;

    @Autowired
    private QueryRepository repository;

    public static record QueryInput(
        Id categoryId,
        String queryString
    ) {}

    public static record QueryResult(
        List<String> rows
    ) {}

    @PostMapping("/queries/execute")
    public QueryResult executeQuery(@RequestBody QueryInput data) {
        final var result = service.executeQuery(data.categoryId, data.queryString);

        return new QueryResult(result.toJsonArray());
    }

    @GetMapping("/schema-categories/{categoryId}/queries")
    public List<QueryWithVersion> getQueriesInCategory(@PathVariable Id categoryId) {
        return repository.findAllInCategory(categoryId);
    }

    @GetMapping("/queries/{queryId}")
    public QueryWithVersion getQuery(@PathVariable Id queryId) {
        return repository.find(queryId);
    }

    private static record QueryWithVersions(
        Query query,
        List<QueryVersion> versions
    ) {}

    @GetMapping("/queries/{queryId}/with-versions")
    public QueryWithVersions getQueryWithVersions(@PathVariable Id queryId) {
        final var queryWithVersion = repository.find(queryId);
        final var versions = repository.findAllVersionsByQuery(queryId);

        return new QueryWithVersions(queryWithVersion.query(), versions);
    }

    public static record QueryInit(
        Id categoryId,
        String label,
        String content
    ) {}

    @PostMapping("/queries")
    public QueryWithVersion createQuery(@RequestBody QueryInit init) {
        return service.createQuery(init);
    }

    @DeleteMapping("/queries/{queryId}")
    public void deleteQuery(@PathVariable Id queryId) {
        boolean result = service.deleteQueryWithVersions(queryId);
        if (!result)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public static record QueryVersionInit(
        Version version,
        String content
    ) {}

    @PostMapping("/queries/{queryId}/versions")
    public QueryVersion createQueryVersion(@PathVariable Id queryId, @RequestBody QueryVersionInit init) {
        return service.createQueryVersion(queryId, init);
    }

}
