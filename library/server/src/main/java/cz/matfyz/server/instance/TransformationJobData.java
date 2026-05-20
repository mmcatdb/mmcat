package cz.matfyz.server.instance;

import cz.matfyz.server.job.JobData;
import cz.matfyz.server.utils.entity.Id;

public record TransformationJobData(
    Id fileId
) implements JobData {}
