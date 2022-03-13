package cz.cuni.matfyz.server.entity;

import org.json.JSONException;
import org.json.JSONObject;

import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;
import cz.cuni.matfyz.core.serialization.UniqueContext;

/**
 * 
 * @author jachym.bartik
 */
public class SchemaMorphismWrapper // implements JSONConvertible
{
    public final int id;
    //public final JSONObject jsonValue;
    public final int domId;
    public final int codId;
    public final String jsonValue;

    public SchemaMorphismWrapper(int id, int domId, int codId, String jsonValue)
    {
        this.id = id;
        this.domId = domId;
        this.codId = codId;
        this.jsonValue = jsonValue;
    }

    public SchemaMorphism toSchemaMorphism(UniqueContext<SchemaObject, Key> context) throws Exception // TODO
    {
        //return new SchemaMorphism.Builder(context).fromJSON(jsonValue);
        return new SchemaMorphism.Builder(context).fromJSON(new JSONObject(jsonValue));
    }

    /*
    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<SchemaMorphismWrapper> {

        @Override
        protected JSONObject _toJSON(SchemaMorphismWrapper object) throws JSONException {
            var output = new JSONObject();

			output.put("id", object.id);
            output.put("value", object.jsonValue);

            return output;
        }

	}
    */
}
