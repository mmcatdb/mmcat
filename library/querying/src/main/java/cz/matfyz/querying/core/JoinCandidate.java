package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.JoinCondition;
import cz.matfyz.core.utils.GraphUtils.Edge;
import cz.matfyz.querying.core.patterntree.PatternForKind;

import java.util.List;

public record JoinCandidate(
    JoinType type,
    /** The kind with the id (if we join as IdRef). */
    PatternForKind from,
    /** The kind with the reference (if we join as IdRef). */
    PatternForKind to,
    List<JoinCondition> joinProperties,
    int recursion, // Some DBs allow to recursively join the same kind.
    boolean isOptional
) implements Edge<PatternForKind> {

    public enum JoinType {
        IdRef,
        Value,
    }

    public boolean isRecursive() {
        return recursion > 0;
    }

}
