package cz.matfyz.core.adminer;

/**
 * The Reference record represents a reference that connects two kind in a database.
 */
public record Reference(
    ReferenceKind from,
    ReferenceKind to
) {

    /**
     * Represents a kind that is included in the reference.
     */
    public record ReferenceKind(
        String datasourceId,
        String kindName,
        String property
    ) {}

}
