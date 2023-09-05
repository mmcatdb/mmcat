package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.core.mapping.AccessPath;

import java.util.List;

public record JoinCandidate(
    JoinType type,
    Kind from,
    Kind to,
    List<JoinProperty> joinProperties,
    int recursion // Some DBs allow to recursively join the same kind.
) {

    public static record JoinProperty(
        AccessPath from,
        AccessPath to
    ) {}

    public enum JoinType {
        IdRef,
        Value,
    }

}
