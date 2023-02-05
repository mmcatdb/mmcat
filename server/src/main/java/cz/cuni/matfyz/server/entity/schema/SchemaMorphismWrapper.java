package cz.cuni.matfyz.server.entity.schema;

import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public class SchemaMorphismWrapper extends Entity {

    public final Id domId;
    public final Id codId;
    public final String jsonValue;

    public SchemaMorphismWrapper(Id id, Id domId, Id codId, String jsonValue) {
        super(id);
        this.domId = domId;
        this.codId = codId;
        this.jsonValue = jsonValue;
    }

}
