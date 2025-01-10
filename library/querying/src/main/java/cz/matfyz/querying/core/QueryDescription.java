package cz.matfyz.querying.core;

import cz.matfyz.core.querying.QueryStructure;
import cz.matfyz.querying.core.querytree.QueryNode.SerializedQueryNode;

import java.util.List;

public record QueryDescription(
    List<QueryPartDescription> parts,
    SerializedQueryNode tree
) {

    public record QueryPartDescription(
        String datasourceIdentifier,
        QueryStructure structure,
        String content
    ) {}

}
