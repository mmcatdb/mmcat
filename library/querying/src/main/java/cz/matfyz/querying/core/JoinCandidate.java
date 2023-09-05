package cz.matfyz.querying.core;

import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.Kind;

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
