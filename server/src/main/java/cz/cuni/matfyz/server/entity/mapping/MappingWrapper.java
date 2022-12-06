package cz.cuni.matfyz.server.entity.mapping;

import cz.cuni.matfyz.server.entity.Entity;

/**
 * @author jachym.bartik
 */
public class MappingWrapper extends Entity {

    public final int logicalModelId;
    public final int rootObjectId;
    public final String mappingJsonValue;
    public final String jsonValue;

    public MappingWrapper(Integer id, int logicalModelId, int rootObjectId, String mappingJsonValue, String jsonValue) {
        super(id);
        this.logicalModelId = logicalModelId;
        this.rootObjectId = rootObjectId;
        this.mappingJsonValue = mappingJsonValue;
        this.jsonValue = jsonValue;
    }

}
