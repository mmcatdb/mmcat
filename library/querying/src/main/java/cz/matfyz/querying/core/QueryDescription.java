package cz.matfyz.querying.core;

import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.querying.core.querytree.QueryNode.SerializedQueryNode;

import java.util.List;

public record QueryDescription(
    QueryPlanDescription planned,
    QueryPlanDescription optimized
) {

    public record QueryPlanDescription(
        List<QueryPartDescription> parts,
        SerializedQueryNode tree
    ) {}

    public record QueryPartDescription(
        String datasourceIdentifier,
        ResultStructure structure,
        String content
    ) {}

}
