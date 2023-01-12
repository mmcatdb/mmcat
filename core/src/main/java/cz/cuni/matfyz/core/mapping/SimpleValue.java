package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple value is a signature of morphism ?(which maps the parent property to this value)?
 * @author jachymb.bartik
 */
public class SimpleValue implements JSONConvertible {
    
    private final Signature signature;
    
    public Signature signature() {
        return signature;
    }
    
    public SimpleValue(Signature signature) {
        this.signature = signature;
    }
    
    private static final SimpleValue empty = new SimpleValue(Signature.createEmpty());
    
    public static SimpleValue createEmpty() {
        return empty;
    }

    public boolean isEmpty() {
        return this.signature.isEmpty();
    }
    
    @Override
    public String toString() {
        return signature.toString();
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<SimpleValue> {

        @Override
        protected JSONObject innerToJSON(SimpleValue object) throws JSONException {
            var output = new JSONObject();
    
            output.put("signature", object.signature.toJSON());
            
            return output;
        }
    
    }
    
    public static class Builder extends FromJSONBuilderBase<SimpleValue> {
    
        @Override
        protected SimpleValue innerFromJSON(JSONObject jsonObject) throws JSONException {
            var signature = new Signature.Builder().fromJSON(jsonObject.getJSONArray("signature"));
            
            return new SimpleValue(signature);
        }
    
    }
}
