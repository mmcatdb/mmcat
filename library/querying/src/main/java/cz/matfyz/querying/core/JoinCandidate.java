package cz.matfyz.querying.core;

import cz.matfyz.core.mapping.Kind;
import cz.matfyz.core.mapping.SimpleProperty;

import java.util.List;

public record JoinCandidate(
    Kind from,
    Kind to,
    List<JoinProperty> joinProperties,
    int recursion // Some DBs allow to recursively join the same kind.
) {

    public static record JoinProperty(
        SimpleProperty from,
        SimpleProperty to
    ) {}

}
