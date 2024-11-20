package cz.matfyz.server.entity.action.payload;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.job.JobPayload;

public record UpdateSchemaPayload(
    Version prevVersion,
    Version nextVersion
) implements JobPayload {}
