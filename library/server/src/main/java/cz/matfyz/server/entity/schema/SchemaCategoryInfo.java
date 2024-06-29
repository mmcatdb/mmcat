package cz.matfyz.server.entity.schema;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

public class SchemaCategoryInfo extends Entity {

    public final String label;
    public final Version version;

    public SchemaCategoryInfo(Id id, String label, Version version) {
        super(id);
        this.label = label;
        this.version = version;
    }

    public static SchemaCategoryInfo fromWrapper(SchemaCategoryWrapper wrapper) {
        return new SchemaCategoryInfo(wrapper.id, wrapper.label, wrapper.version);
    }

}
