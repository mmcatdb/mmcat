package cz.cuni.matfyz.server.entity.schema;

import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public class SchemaCategoryInfo extends Entity {

    public final String jsonValue;

    public SchemaCategoryInfo(Id id, String jsonValue) {
        super(id);
        this.jsonValue = jsonValue;
    }

}
