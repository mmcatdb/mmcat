package cz.cuni.matfyz.core.schema;

import org.json.JSONException;
import org.json.JSONObject;

import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;

/**
 * This class represents a 'key' of an object as is described in the paper. It's basically just a number with extra steps.
 * @author pavel.koupil, jachym.bartik
 */
public class Key implements Comparable<Key>, JSONConvertible
{
    private final int value;
    
    public int getValue()
    {
        return value;
    }

	public Key(int value)
    {
		this.value = value;
	}

	@Override
	public int compareTo(Key key)
    {
		return value - key.value;
	}
    
    @Override
	public String toString()
    {
		return value + "";
	}
    
    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Key key && compareTo(key) == 0;
    }

    /**
     * Auto-generated, constants doesn't have any special meaning.
     * @return 
     */
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 29 * hash + this.value;
        return hash;
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<Key> {

        @Override
        protected JSONObject _toJSON(Key object) throws JSONException {
            var output = new JSONObject();
    
            output.put("value", object.value);
            
            return output;
        }
    
    }
    
    public static class Builder extends FromJSONBuilderBase<Key> {
    
        @Override
        protected Key _fromJSON(JSONObject jsonObject) throws JSONException {
            var value = jsonObject.getInt("value");

            return new Key(value);
        }
    
    }
}
