package cz.matfyz.server.entity.job.payload;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DataSource;
import cz.matfyz.server.entity.job.JobPayload;

/**
 * @author jachym.bartik
 */
public record JsonLdToCategoryPayload(
    Id dataSourceId
) implements JobPayload {

    public static record Detail(
        DataSource dataSource
    ) implements JobPayload.Detail {}

}
