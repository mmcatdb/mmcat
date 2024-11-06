package cz.matfyz.server.entity.action.payload;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.ActionPayload;

import java.util.List;

public record RSDToCategoryPayload(
    List<Id> datasourceIds
) implements ActionPayload {}

