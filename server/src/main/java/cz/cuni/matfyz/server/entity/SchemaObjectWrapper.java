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
public class SchemaObjectWrapper // implements JSONConvertible
{
    public final int id;
    //public final JSONObject jsonValue;
    public final String jsonValue;
    public final Position position;

    //public SchemaObjectWrapper(int id, JSONObject jsonValue)
    public SchemaObjectWrapper(int id, String jsonValue, Position position)
    {
        this.id = id;
        this.jsonValue = jsonValue;
        this.position = position;
    }

    public SchemaObject toSchemaObject() throws Exception
    {
        //return new SchemaObject.Builder().fromJSON(jsonValue);
        return new SchemaObject.Builder().fromJSON(new JSONObject(jsonValue)); //TODO
    }
/*
    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<SchemaObjectWrapper> {

        @Override
        protected JSONObject _toJSON(SchemaObjectWrapper object) throws JSONException {
            var output = new JSONObject();

			output.put("id", object.id);
            output.put("value", object.jsonValue);

            return output;
        }

	}
    */
}
