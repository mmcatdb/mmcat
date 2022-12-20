package cz.cuni.matfyz.server.entity.schema;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.Entity;

/**
 * @author jachym.bartik
 */
public class SchemaCategoryInfoBetter extends Entity {

    public final String jsonValue;
    public final Id idBetter;

    public SchemaCategoryInfoBetter(SchemaCategoryInfo info) {
        super(info.id);
        this.jsonValue = info.jsonValue;
        this.idBetter = new Id("" + info.id);
    }

}
