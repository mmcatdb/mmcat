package cz.matfyz.server.entity.action.payload;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.job.JobPayload;

import java.util.List;

public record ModelToCategoryPayload(
    Id datasourceId,
    /** If not empty, only the selected mappings from this datasource will be used. */
    List<Id> mappingIds
) implements JobPayload {}
