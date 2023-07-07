package cz.cuni.matfyz.server.entity.job;

import cz.cuni.matfyz.server.entity.IEntity;
import cz.cuni.matfyz.server.entity.Id;

import java.io.Serializable;

import org.springframework.lang.Nullable;

/**
 * @author jachym.bartik
 */
public record JobDetail(
    Id id,
    Id categoryId,
    String label,
    Job.State state,
    JobPayload.Detail payload,
    @Nullable Serializable data
) implements IEntity {

    public JobDetail(Job job, JobPayload.Detail payload) {
        this(
            job.id,
            job.categoryId,
            job.label,
            job.state,
            payload,
            job.data
        );
    }

}
