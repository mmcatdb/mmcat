package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.utils.JSONConverterBase;
import cz.cuni.matfyz.core.utils.JSONConvertible;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * A simple value is a signature of morphism ?(which maps the parent property to this value)?
 * @author jachymb.bartik
 */
public class SimpleValue implements IValue, JSONConvertible
{
    private final Signature signature;
    
    public Signature signature()
    {
        return signature;
    }
    
    public SimpleValue(Signature signature)
    {
        this.signature = signature;
    }
    
    private final static SimpleValue empty = new SimpleValue(Signature.Empty());
    
    public static SimpleValue Empty()
    {
        return empty;
    }
    
    @Override
	public String toString()
    {
        return signature.toString();
	}

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends JSONConverterBase<SimpleValue> {

        @Override
        protected JSONObject _toJSON(SimpleValue object) throws JSONException {
            var output = new JSONObject();

            output.put("signature", object.signature.toJSON());

            return output;
        }

        @Override
        protected SimpleValue _fromJSON(JSONObject jsonObject) throws JSONException {
            var signature = new Signature.Converter().fromJSON(jsonObject.getJSONObject("signature"));
            
            return new SimpleValue(signature);
        }

    }
}
