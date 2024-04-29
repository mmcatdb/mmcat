package cz.matfyz.server.entity.evolution;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper.MetadataUpdate;

import java.util.List;

public record SchemaUpdateInit(
    Version prevVersion,
    List<VersionedSMO> operations,
    List<MetadataUpdate> metadata
) {}
