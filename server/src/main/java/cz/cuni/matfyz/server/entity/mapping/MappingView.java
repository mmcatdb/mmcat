package cz.cuni.matfyz.server.entity.mapping;

import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.database.DatabaseView;

/**
 * 
 * @author jachym.bartik
 */
public class MappingView extends Entity {

    public final DatabaseView databaseView;
    public final int categoryId;
    public final Integer rootObjectId;
    public final Integer rootMorphismId;
    public final String mappingJsonValue;
    public final String jsonValue;

    public MappingView(MappingWrapper wrapper, DatabaseView databaseView) {
        super(wrapper.id);
        this.databaseView = databaseView;
        this.categoryId = wrapper.categoryId;
        this.rootObjectId = wrapper.rootObjectId;
        this.rootMorphismId = wrapper.rootMorphismId;
        this.mappingJsonValue = wrapper.mappingJsonValue;
        this.jsonValue = wrapper.jsonValue;
    }

}
