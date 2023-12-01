package cz.matfyz.server.example.common;

import cz.matfyz.server.controller.QueryController.QueryInit;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.QueryRepository.QueryWithVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class QueryBuilder {
    
    private final List<QueryInit> inits = new ArrayList<>();
    private final Id schemaId;

    public QueryBuilder(Id schemaId) {
        this.schemaId = schemaId;
    }

    public QueryBuilder add(String label, String content) {
        inits.add(new QueryInit(schemaId, label, content));

        return this;
    }

    public List<QueryWithVersion> build(Function<QueryInit, QueryWithVersion> creator) {
        return inits.stream().map(creator).toList();
    }

}
