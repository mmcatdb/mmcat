package cz.matfyz.server.example.common;

import cz.matfyz.server.querying.Query;
import cz.matfyz.server.querying.QueryStats;
import cz.matfyz.server.querying.QueryController.QueryInit;
import cz.matfyz.server.utils.entity.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.checkerframework.checker.nullness.qual.Nullable;

public class QueryBuilder {

    private final List<QueryInit> inits = new ArrayList<>();
    private final List<QueryStats> statsInits = new ArrayList<>();
    private final Id categoryId;

    public QueryBuilder(Id categoryId) {
        this.categoryId = categoryId;
    }

    public QueryBuilder add(String label, String content) {
        return add(label, content, null);
    }

    public QueryBuilder add(String label, String content, @Nullable QueryStats stats) {
        inits.add(new QueryInit(categoryId, label, content));
        statsInits.add(stats);

        return this;
    }

    public List<Query> build(BiFunction<QueryInit, QueryStats, Query> creator) {
        final List<Query> queries = new ArrayList<>();
        for (int i = 0; i < inits.size(); i++)
            queries.add(creator.apply(inits.get(i), statsInits.get(i)));

        return queries;
    }

}
