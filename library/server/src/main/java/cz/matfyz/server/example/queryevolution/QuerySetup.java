package cz.matfyz.server.example.queryevolution;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.service.QueryService;
import cz.matfyz.tests.example.queryevolution.Queries;
import cz.matfyz.server.example.common.QueryBuilder;
import cz.matfyz.server.repository.QueryRepository.QueryWithVersion;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("queryEvolutionQuerySetup")
class QuerySetup {

    @Autowired
    private QueryService queryService;

    List<QueryWithVersion> createQueries(Id categoryId) {
        return new QueryBuilder(categoryId)
            .add("Find friends", Queries.findFriends)
            .add("Most expensive order", Queries.mostExpensiveOrder)
            .build(queryService::createQuery);
    }

}
