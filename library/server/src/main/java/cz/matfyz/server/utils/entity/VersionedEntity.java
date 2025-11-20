package cz.matfyz.server.utils.entity;

import cz.matfyz.evolution.Version;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents entity which both (a) has a version and (b) lives in a system with other versioned entities.
 * The global version of the system (i.e., the "system version") is the maximal version of all its entities.
 */
public class VersionedEntity extends Entity {

    private Version version;
    /** The last system version for which is this entity still valid. */
    private Version lastValid;

    public VersionedEntity(Id id, Version version, Version lastValid) {
        super(id);
        this.version = version;
        this.lastValid = lastValid;
    }

    // Generally, whenever we update version of an entity A to V_n, we have to:
    //  - Update the system version to V_n.
    //  - Find all entities B that don't depend on A and have lastValid >= V_n-1. We then update their lastValid to max(lastValid, V_n).
    //      - Usually, it should be lastValid = V_n. However, if we were to introduce something like batch updates, the >= might be necessary.
    //
    // Later, we should find all entities with lastValid < systemVersion and schedule them for automatic/manual propagation.

    public void updateVersion(Version newVersion, Version oldSystemVersion) {
        version = newVersion;

        if (lastValid.isAfterOrEqual(oldSystemVersion))
            lastValid = lastValid.isAfterOrEqual(newVersion) ? lastValid : newVersion;
    }

    public void updateLastValid(Version newLastValid) {
        lastValid = newLastValid;
    }

    @JsonProperty("version")
    public Version version() {
        return version;
    }

    @JsonProperty("lastValid")
    public Version lastValid() {
        return lastValid;
    }

}
