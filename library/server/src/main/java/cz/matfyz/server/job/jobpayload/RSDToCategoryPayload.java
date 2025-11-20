package cz.matfyz.server.job.jobpayload;

import cz.matfyz.server.utils.entity.Id;

import java.util.List;

public record RSDToCategoryPayload(
    List<Id> datasourceIds
) implements JobPayload {}

