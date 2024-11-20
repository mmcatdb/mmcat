package cz.matfyz.server.entity.action.payload;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.job.JobPayload;

import java.util.List;

public record RSDToCategoryPayload(
    List<Id> datasourceIds
) implements JobPayload {}

