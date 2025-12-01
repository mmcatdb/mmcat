package cz.matfyz.server.instance;

import cz.matfyz.server.job.JobPayload;
import cz.matfyz.server.utils.entity.Id;

import java.util.List;

public record CategoryToModelPayload(
    Id datasourceId,
    /** If not empty, only the selected mappings from this datasource will be used. */
    List<Id> mappingIds
) implements JobPayload {}
