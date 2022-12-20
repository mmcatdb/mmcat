package cz.cuni.matfyz.server.entity.mapping;

import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public class MappingWrapper extends Entity {

    public final Id logicalModelId;
    public final Id rootObjectId;
    public final String mappingJsonValue;
    public final String jsonValue;

    public MappingWrapper(Id id, Id logicalModelId, Id rootObjectId, String mappingJsonValue, String jsonValue) {
        super(id);
        this.logicalModelId = logicalModelId;
        this.rootObjectId = rootObjectId;
        this.mappingJsonValue = mappingJsonValue;
        this.jsonValue = jsonValue;
    }

}
