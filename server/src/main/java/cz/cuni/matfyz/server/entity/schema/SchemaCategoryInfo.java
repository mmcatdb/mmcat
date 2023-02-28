package cz.cuni.matfyz.server.entity.schema;

import cz.cuni.matfyz.evolution.Version;
import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.Id;

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
