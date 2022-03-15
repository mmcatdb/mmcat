package cz.cuni.matfyz.server.entity;

import org.json.JSONException;
import org.json.JSONObject;

import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

/**
 * 
 * @author jachym.bartik
 */
public class MappingWrapper
{
    public final int id;
    public final int schemaId;
    public final int databaseId;
    public final Integer rootObjectId;
    public final Integer rootMorphismId;
    //public final JSONObject jsonValue;
    public final String jsonValue;

    //public SchemaObjectWrapper(int id, JSONObject jsonValue)
    public MappingWrapper(int id, int schemaId, int databaseId, Integer rootObjectId, Integer rootMorphismId, String jsonValue)
    {
        this.id = id;
        this.schemaId = schemaId;
        this.databaseId = databaseId;
        this.rootObjectId = rootObjectId;
        this.rootMorphismId = rootMorphismId;
        this.jsonValue = jsonValue;
    }
}
