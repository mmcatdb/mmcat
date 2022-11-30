package cz.cuni.matfyz.server.entity.mapping;

import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModelView;

/**
 * @author jachym.bartik
 */
public class MappingView extends Entity {

    public final LogicalModelView logicalModelView;
    public final Integer rootObjectId;
    public final Integer rootMorphismId;
    public final String mappingJsonValue;
    public final String jsonValue;

    public MappingView(MappingWrapper wrapper, LogicalModelView logicalModelView) {
        super(wrapper.id);
        this.logicalModelView = logicalModelView;
        this.rootObjectId = wrapper.rootObjectId;
        this.rootMorphismId = wrapper.rootMorphismId;
        this.mappingJsonValue = wrapper.mappingJsonValue;
        this.jsonValue = wrapper.jsonValue;
    }

}
