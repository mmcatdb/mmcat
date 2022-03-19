package cz.cuni.matfyz.server.entity;

/**
 * 
 * @author jachym.bartik
 */
public class MappingWrapper extends Entity {

    public final int databaseId;
    public final int categoryId;
    public final Integer rootObjectId;
    public final Integer rootMorphismId;
    public final String jsonValue;

    public MappingWrapper(Integer id, int databaseId, int categoryId, Integer rootObjectId, Integer rootMorphismId, String jsonValue) {
        super(id);
        this.databaseId = databaseId;
        this.categoryId = categoryId;
        this.rootObjectId = rootObjectId;
        this.rootMorphismId = rootMorphismId;
        this.jsonValue = jsonValue;
    }

}
