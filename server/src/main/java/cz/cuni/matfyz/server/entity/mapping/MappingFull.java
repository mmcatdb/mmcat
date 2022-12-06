package cz.cuni.matfyz.server.entity.mapping;

import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModelInfo;

/**
 * @author jachym.bartik
 */
public class MappingFull extends Entity {

    public final LogicalModelInfo logicalModel;
    public final int rootObjectId;
    public final String mappingJsonValue;
    public final String jsonValue;

    public MappingFull(int id, LogicalModelInfo logicalModel, int rootObjectId, String mappingJsonValue, String jsonValue) {
        super(id);
        this.logicalModel = logicalModel;
        this.rootObjectId = rootObjectId;
        this.mappingJsonValue = mappingJsonValue;
        this.jsonValue = jsonValue;
    }

}
