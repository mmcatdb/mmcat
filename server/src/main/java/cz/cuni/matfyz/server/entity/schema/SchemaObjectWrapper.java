package cz.cuni.matfyz.server.entity.schema;

import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.utils.Position;

/**
 * @author jachym.bartik
 */
public class SchemaObjectWrapper extends Entity {

    public final String jsonValue;
    public final Position position;
    
    public SchemaObjectWrapper(Id id, String jsonValue, Position position) {
        super(id);
        this.jsonValue = jsonValue;
        this.position = position;
    }
   
}
