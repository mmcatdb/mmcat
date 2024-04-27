package cz.matfyz.server.entity.evolution;

public record VersionedSMO(
    String version,
    SchemaModificationOperation smo
) {}
