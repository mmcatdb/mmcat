package cz.matfyz.server.entity.job;

import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.Nullable;

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
