package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.database.Database;
import cz.matfyz.abstractwrappers.database.Kind;

import java.util.List;
import java.util.Set;

public record QueryPart(
    Database database,
    Set<Kind> kinds,
    List<JoinCandidate> joinCandidates
) {

    /**
     * Query part for a single kind.
     */
    public static QueryPart create(Kind kind) {
        return create(Set.of(kind), List.of());
    }

    /**
     * Complex query part with multiple kinds and joins between them.
     */
    public static QueryPart create(Set<Kind> kinds, List<JoinCandidate> candidates) {
        final var database = kinds.stream().findFirst().get().database;

        return new QueryPart(database, kinds, candidates);
    }

}