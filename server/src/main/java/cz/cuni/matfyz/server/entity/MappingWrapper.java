package cz.cuni.matfyz.server.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author jachym.bartik
 */
public class MappingWrapper extends Entity {

    public final int databaseId;
    public final int categoryId;
    public final Integer rootObjectId;
    public final Integer rootMorphismId;
    public final String mappingJsonValue;
    public final String jsonValue;

    @JsonCreator
    public MappingWrapper(
        @JsonProperty("id") Integer id,
        @JsonProperty("databaseId") int databaseId,
        @JsonProperty("categoryId") int categoryId,
        @JsonProperty("rootObjectId") Integer rootObjectId,
        @JsonProperty("rootMorphismId") Integer rootMorphismId,
        @JsonProperty("mappingJsonValue") String mappingJsonValue,
        @JsonProperty("jsonValue") String jsonValue
    ) {
        super(id);
        this.databaseId = databaseId;
        this.categoryId = categoryId;
        this.rootObjectId = rootObjectId;
        this.rootMorphismId = rootMorphismId;
        this.mappingJsonValue = mappingJsonValue;
        this.jsonValue = jsonValue;
    }

}
