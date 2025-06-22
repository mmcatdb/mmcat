package cz.matfyz.core.adminer;

/**
 * The ReferenceKind record represents a kind that is included in a reference.
 */
public record ReferenceKind(
    String datasourceId,
    String kindName,
    String property
) {}
