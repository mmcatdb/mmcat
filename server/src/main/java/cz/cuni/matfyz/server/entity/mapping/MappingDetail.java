package cz.cuni.matfyz.server.entity.mapping;

import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.schema.SchemaObjectWrapper;

/**
 * @author jachym.bartik
 */
public class MappingDetail extends Entity {

    public final Id logicalModelId;
    public final SchemaObjectWrapper rootObject;
    public final String mappingJsonValue;
    public final String jsonValue;

    public MappingDetail(MappingWrapper mapping, SchemaObjectWrapper rootObject) {
        super(mapping.id);
        this.logicalModelId = mapping.logicalModelId;
        this.rootObject = rootObject;
        this.mappingJsonValue = mapping.mappingJsonValue;
        this.jsonValue = mapping.jsonValue;
    }

}
