package cz.matfyz.server.inference;

import cz.matfyz.server.job.JobPayload;
import cz.matfyz.server.utils.entity.Id;

import java.util.List;

public record InferencePayload(
    List<Id> datasourceIds
) implements JobPayload {

    @Override public boolean isFinishedAutomatically() {
        return false;
    }

}
