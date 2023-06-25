package cz.cuni.matfyz.server.entity.job;

import cz.cuni.matfyz.server.entity.IEntity;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.datasource.DataSource;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModelInfo;

import org.springframework.lang.Nullable;

/**
 * @author jachym.bartik
 */
public record JobDetail(
    Id id,
    Id categoryId,
    @Nullable LogicalModelInfo logicalModel,
    @Nullable DataSource dataSource,
    String label,
    Job.Type type,
    Job.State state
) implements IEntity {

    public JobDetail(Job job, LogicalModelInfo logicalModel) {
        this(
            job.id,
            job.categoryId,
            logicalModel,
            null,
            job.label,
            job.type,
            job.state
        );
    }

    public JobDetail(Job job, DataSource dataSource) {
        this(
            job.id,
            job.categoryId,
            null,
            dataSource,
            job.label,
            job.type,
            job.state
        );
    }

}
