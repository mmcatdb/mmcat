package cz.matfyz.querying.core;

import cz.matfyz.core.mapping.Kind;

public record JoinCandidate(
    Kind from,
    Kind to,
    Object condition, // TODO what is join condition?
    int times
) {}
