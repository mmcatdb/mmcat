package cz.matfyz.server.entity.action.payload;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.action.ActionPayload;

public record UpdateSchemaPayload(
    Version prevVersion,
    Version nextVersion
) implements ActionPayload {}
