package cz.cuni.matfyz.server.entity.mapping;

import cz.cuni.matfyz.server.entity.Entity;

/**
 * @author jachym.bartik
 */
public class MappingWrapper extends Entity {

    public final int logicalModelId;
    public final Integer rootObjectId;
    public final Integer rootMorphismId;
    public final String mappingJsonValue;
    public final String jsonValue;

    public MappingWrapper(Integer id, int logicalModelId, Integer rootObjectId, Integer rootMorphismId, String mappingJsonValue, String jsonValue) {
        super(id);
        this.logicalModelId = logicalModelId;
        this.rootObjectId = rootObjectId;
        this.rootMorphismId = rootMorphismId;
        this.mappingJsonValue = mappingJsonValue;
        this.jsonValue = jsonValue;
    }

}
