package cz.matfyz.server.example.common;

import cz.matfyz.server.controller.QueryController.QueryInit;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class QueryBuilder {

    private final List<QueryInit> inits = new ArrayList<>();
    private final Id categoryId;

    public QueryBuilder(Id categoryId) {
        this.categoryId = categoryId;
    }

    public QueryBuilder add(String label, String content) {
        inits.add(new QueryInit(categoryId, label, content));

        return this;
    }

    public List<Query> build(Function<QueryInit, Query> creator) {
        return inits.stream().map(creator).toList();
    }

}
