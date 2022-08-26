package cz.cuni.matfyz.server.entity.schema;

import cz.cuni.matfyz.server.entity.Entity;

/**
 * @author jachym.bartik
 */
public class SchemaMorphismWrapper extends Entity { // implements JSONConvertible

    //public final JSONObject jsonValue;
    public final int domId;
    public final int codId;
    public final String jsonValue;

    public SchemaMorphismWrapper(Integer id, int domId, int codId, String jsonValue) {
        super(id);
        this.domId = domId;
        this.codId = codId;
        this.jsonValue = jsonValue;
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
