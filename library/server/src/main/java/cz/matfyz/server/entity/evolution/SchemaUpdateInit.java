package cz.matfyz.server.entity.evolution;

import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.metadata.MetadataModificationOperation;

import java.util.List;

public record SchemaUpdateInit(
    Version prevVersion,
    List<VersionedSMO> operations,
    List<MetadataModificationOperation> metadata
) {}
