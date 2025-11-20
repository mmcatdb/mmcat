package cz.matfyz.server.example.queryevolution;

import cz.matfyz.tests.example.queryevolution.Queries;
import cz.matfyz.server.example.common.QueryBuilder;
import cz.matfyz.server.querying.Query;
import cz.matfyz.server.querying.QueryService;
import cz.matfyz.server.utils.entity.Id;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("queryEvolutionQuerySetup")
class QuerySetup {

    @Autowired
    private QueryService queryService;

    List<Query> createQueries(Id categoryId) {
        return new QueryBuilder(categoryId)
            .add("Find friends", Queries.findFriends)
            .add("Most expensive order", Queries.mostExpensiveOrder)
            .build(queryService::create);
    }

}
