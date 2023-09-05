package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.database.Kind;

import java.util.List;

public record QueryPart2(
    List<Kind> kinds
) {}