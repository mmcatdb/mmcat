package cz.cuni.matfyz.server.entity;

import cz.cuni.matfyz.server.utils.Position;

/**
 * 
 * @author jachym.bartik
 */
public class SchemaObjectWrapper extends Entity { // implements JSONConvertible

    //public final JSONObject jsonValue;
    public final String jsonValue;
    public final Position position;
    
    //public SchemaObjectWrapper(int id, JSONObject jsonValue)
    public SchemaObjectWrapper(Integer id, String jsonValue, Position position) {
        super(id);
        this.jsonValue = jsonValue;
        this.position = position;
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
