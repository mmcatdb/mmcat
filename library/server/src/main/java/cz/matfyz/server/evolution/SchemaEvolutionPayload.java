package cz.matfyz.server.evolution;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.job.JobPayload;

public record SchemaEvolutionPayload(
    Version prevVersion,
    Version nextVersion
) implements JobPayload {

    @Override public boolean isStartedAutomatically() {
        return false;
    }

}
