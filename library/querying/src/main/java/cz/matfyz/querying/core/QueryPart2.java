package cz.matfyz.querying.core;

import cz.matfyz.core.mapping.Kind;

import java.util.List;

public record QueryPart2(
    List<Kind> kinds
) {}