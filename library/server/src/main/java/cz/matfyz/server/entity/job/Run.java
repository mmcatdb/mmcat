package cz.matfyz.server.entity.job;

import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

import org.checkerframework.checker.nullness.qual.Nullable;

public class Run extends Entity {

    public final Id categoryId;
    /** The action that was used to generate this run. It might be null because not all runs are generated by user actions. */
    public final @Nullable Id actionId;
    /** The user session that was active when this run was created. Runs not generated by user actions don't have a session. */
    public final @Nullable Id sessionId;

    private Run(Id id, Id categoryId, @Nullable Id actionId, @Nullable Id sessionId) {
        super(id);
        this.categoryId = categoryId;
        this.actionId = actionId;
        this.sessionId = sessionId;
    }

    public static Run createSystem(Id categoryId) {
        return new Run(
            Id.createNewUUID(),
            categoryId,
            null,
            null
        );
    }

    public static Run createUser(Id categoryId, Id actionId, Id sessionId) {
        return new Run(
            Id.createNewUUID(),
            categoryId,
            actionId,
            sessionId
        );
    }

    public static Run fromDatabase(Id id, Id categoryId, @Nullable Id actionId, @Nullable Id sessionId) {
        return new Run(
            id,
            categoryId,
            actionId,
            sessionId
        );
    }

}
