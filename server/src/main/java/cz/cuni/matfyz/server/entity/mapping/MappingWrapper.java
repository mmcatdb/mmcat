package cz.cuni.matfyz.server.entity.mapping;

import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.schema.SchemaObjectWrapper;

/**
 * @author jachym.bartik
 */
public class MappingWrapper extends Entity {

    public final Id logicalModelId;
    public final SchemaObjectWrapper rootObject;
    public final String mappingJsonValue;
    public final String jsonValue;

    public MappingWrapper(Id id, Id logicalModelId, SchemaObjectWrapper rootObject, String mappingJsonValue, String jsonValue) {
        super(id);
        this.logicalModelId = logicalModelId;
        this.rootObject = rootObject;
        this.mappingJsonValue = mappingJsonValue;
        this.jsonValue = jsonValue;
    }

}
