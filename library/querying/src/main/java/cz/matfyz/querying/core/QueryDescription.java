package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;

import java.util.List;

public record QueryDescription(
    List<QueryPartDescription> parts
) {

    public record QueryPartDescription(
        String datasourceIdentifier,
        QueryStatement query
    ) {}

}
