package cz.matfyz.server.job.jobpayload;

import cz.matfyz.evolution.Version;

public record UpdateSchemaPayload(
    Version prevVersion,
    Version nextVersion
) implements JobPayload {}
