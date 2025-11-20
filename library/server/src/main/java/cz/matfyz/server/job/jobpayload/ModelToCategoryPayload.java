package cz.matfyz.server.job.jobpayload;

import cz.matfyz.server.utils.entity.Id;

import java.util.List;

public record ModelToCategoryPayload(
    Id datasourceId,
    /** If not empty, only the selected mappings from this datasource will be used. */
    List<Id> mappingIds
) implements JobPayload {}
