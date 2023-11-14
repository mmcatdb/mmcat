package cz.matfyz.server.controller;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.service.QueryService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jachym.bartik
 */
@RestController
public class QueryController {

    @Autowired
    private QueryService queryService;

    public static record QueryInput(
        Id categoryId,
        String queryString
    ) {}

    public static record QueryResult(
        List<String> rows
    ) {}

    @PostMapping("/queries/execute")
    public QueryResult executeQuery(@RequestBody QueryInput data) {
        final var result = queryService.executeQuery(data.categoryId, data.queryString);

        return new QueryResult(result.toJsonArray());
    }

}
