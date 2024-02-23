package cz.matfyz.server.entity.action.payload;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.ActionPayload;

public record RSDToCategoryPayload(
        Id dataSourceId
) implements ActionPayload {}
