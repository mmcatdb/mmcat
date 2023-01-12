package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.ToJSONSwitchConverterBase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple value node in the access path tree. Its context is undefined (null).
 * @author jachymb.bartik
 */
public class SimpleProperty extends AccessPath {

    private final SimpleValue value;
    
    public SimpleValue value() {
        return value;
    }
    
    public SimpleProperty(Name name, SimpleValue value) {
        super(name);
        
        this.value = value;
    }
    
    public SimpleProperty(String name, Signature value) {
        this(new StaticName(name), new SimpleValue(value));
    }
    
    public SimpleProperty(Signature name, Signature value) {
        this(new DynamicName(name), new SimpleValue(value));
    }
    
    @Override
    protected boolean hasSignature(Signature signature) {
        if (signature == null)
            return value.signature().isEmpty();
        
        return value.signature().equals(signature);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(": ").append(value);
        
        return builder.toString();
    }
    
    @Override
    public Signature signature() {
        return value.signature();
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONSwitchConverterBase<SimpleProperty> {

        @Override
        protected JSONObject innerToJSON(SimpleProperty object) throws JSONException {
            var output = new JSONObject();
            
            output.put("name", object.name.toJSON());
            output.put("value", object.value.toJSON());
            
            return output;
        }
    
    }
    
    public static class Builder extends FromJSONBuilderBase<SimpleProperty> {
    
        @Override
        protected SimpleProperty innerFromJSON(JSONObject jsonObject) throws JSONException {
            var name = new Name.Builder().fromJSON(jsonObject.getJSONObject("name"));
            var value = new SimpleValue.Builder().fromJSON(jsonObject.getJSONObject("value"));
            
            return new SimpleProperty(name, value);
        }
    
    }
}
