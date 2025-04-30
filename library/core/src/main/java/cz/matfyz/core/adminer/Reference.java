package cz.matfyz.core.adminer;

/**
 * The Reference record represents a reference that connects two kind in a database.
 */
public record Reference(
    ReferenceKind from,
    ReferenceKind to
) {}
