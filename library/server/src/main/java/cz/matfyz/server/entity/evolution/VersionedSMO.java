package cz.matfyz.server.entity.evolution;

import cz.matfyz.evolution.schema.SchemaModificationOperation;

public record VersionedSMO(
    String version,
    SchemaModificationOperation smo
) {}
