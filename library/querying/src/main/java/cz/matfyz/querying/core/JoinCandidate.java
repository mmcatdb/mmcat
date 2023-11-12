package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.JoinCondition;
import cz.matfyz.core.utils.GraphUtils.Edge;
import cz.matfyz.querying.core.patterntree.KindPattern;

import java.util.List;

public record JoinCandidate(
    JoinType type,
    KindPattern from,
    KindPattern to,
    List<JoinCondition> joinProperties,
    int recursion, // Some DBs allow to recursively join the same kind.
    boolean isOptional
) implements Edge<KindPattern> {

    public enum JoinType {
        IdRef,
        Value,
    }

    public boolean isRecursive() {
        return recursion > 0;
    }

}
