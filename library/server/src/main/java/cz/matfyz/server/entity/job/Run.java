package cz.matfyz.server.entity.job;

import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a sequence of jobs that are executed in a specific order. Kinda like GitLab's pipeline.
 * Can be generated by a predefined action or manually.
 * The whole run can't be restarted, but the individual jobs can. More precisely, a new job is created under the same run.
 */
public class Run extends Entity {

    public final Id categoryId;
    public final String label;

    /**
     * The action that was used to generate this run. It might be null because not all runs are generated by user actions.
     * Doesn't serve any particular purpose, it's there just for the user's convenience.
     */
    public final @Nullable Id actionId;

    // TODO make non-nullable

    /**
     * The user session that was active when this run was created. It's used for storing intermediate results of jobs.
     */
    public final @Nullable Id sessionId;

    private Run(Id id, Id categoryId, String label, @Nullable Id actionId, @Nullable Id sessionId) {
        super(id);
        this.categoryId = categoryId;
        this.label = label;
        this.actionId = actionId;
        this.sessionId = sessionId;
    }

    public static Run create(Id categoryId, String label, @Nullable Id actionId, @Nullable Id sessionId) {
        return new Run(
            Id.createNew(),
            categoryId,
            label,
            actionId,
            sessionId
        );
    }

    public static Run fromDatabase(Id id, Id categoryId, String label, @Nullable Id actionId, @Nullable Id sessionId) {
        return new Run(
            id,
            categoryId,
            label,
            actionId,
            sessionId
        );
    }

}
