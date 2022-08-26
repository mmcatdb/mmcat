package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.record.DynamicRecordName;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.ToJSONSwitchConverterBase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jachym.bartik
 */
public class DynamicName extends Name {
    
    private final Signature signature;

    public Signature signature() {
        return signature;
    }
    
    public DynamicName(Signature signature) {
        this.signature = signature;
    }
    
    public DynamicRecordName toRecordName(String dynamicNameValue) {
        return new DynamicRecordName(dynamicNameValue, signature);
    }
    
    @Override
    public String toString() {
        return signature.toString();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof DynamicName dynamicName && signature.equals(dynamicName.signature);
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONSwitchConverterBase<DynamicName> {

        @Override
        protected JSONObject innerToJSON(DynamicName object) throws JSONException {
            var output = new JSONObject();
    
            output.put("signature", object.signature.toJSON());
            
            return output;
        }
    
    }
    
    public static class Builder extends FromJSONBuilderBase<DynamicName> {
    
        @Override
        protected DynamicName innerFromJSON(JSONObject jsonObject) throws JSONException {
            var signature = new Signature.Builder().fromJSON(jsonObject.getJSONObject("signature"));
            
            return new DynamicName(signature);
        }
    
    }
}
