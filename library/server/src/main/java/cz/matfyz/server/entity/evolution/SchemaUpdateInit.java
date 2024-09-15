package cz.matfyz.server.entity.evolution;

import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.metadata.MMO;
import cz.matfyz.evolution.schema.SMO;

import java.util.List;

public record SchemaUpdateInit(
    Version prevVersion,
    List<SMO> schema,
    List<MMO> metadata
) {}
