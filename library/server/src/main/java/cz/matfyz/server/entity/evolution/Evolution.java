package cz.matfyz.server.entity.evolution;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

public class Evolution extends Entity {

    public final Id categoryId;
    public final EvolutionType type;
    /** Each evolution represents a system update. As such, it has a unique version. */
    public final Version version;

    protected Evolution(Id id, Id categoryId, Version version, EvolutionType type) {
        super(id);
        this.categoryId = categoryId;
        this.version = version;
        this.type = type;
    }

    public enum EvolutionType {
        schema,
        mapping,
        query,
    }

}
