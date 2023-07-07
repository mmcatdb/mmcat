package cz.cuni.matfyz.server.entity.job.payload;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.job.JobPayload;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModelInfo;

/**
 * @author jachym.bartik
 */
public record CategoryToModelPayload(
    Id logicalModelId
) implements JobPayload {

    public static record Detail(
        LogicalModelInfo logicalModel
    ) implements JobPayload.Detail {}

}
