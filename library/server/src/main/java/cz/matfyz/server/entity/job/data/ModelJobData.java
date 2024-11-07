package cz.matfyz.server.entity.job.data;

import cz.matfyz.server.entity.job.JobData;

public record ModelJobData(
    String value
) implements JobData {}
