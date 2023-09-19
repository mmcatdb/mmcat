package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.JoinCondition;
import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.core.utils.GraphUtils.Edge;

import java.util.List;

public record JoinCandidate(
    JoinType type,
    Kind from,
    Kind to,
    List<JoinCondition> joinProperties,
    int recursion // Some DBs allow to recursively join the same kind.
) implements Edge<Kind> {

    public enum JoinType {
        IdRef,
        Value,
    }

    public boolean isRecursive() {
        return recursion > 0;
    }

    public boolean isOptional() {
        throw new UnsupportedOperationException();
    }

    public Object match() {
        throw new UnsupportedOperationException();
    }

}
