package cz.matfyz.server.entity.schema;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public class SchemaCategoryInfo extends Entity {

    public final String label;
    public final Version version;

    public SchemaCategoryInfo(Id id, String label, Version version) {
        super(id);
        this.label = label;
        this.version = version;
    }

}
