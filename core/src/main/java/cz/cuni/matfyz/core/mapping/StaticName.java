package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.record.StaticRecordName;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author jachym.bartik
 */
public class StaticName extends Name
{
    private final String value;
    private final Type type;
    
    public StaticName(String name)
    {
        super();
        this.value = name;
        this.type = Type.STATIC_NAME;
        this.staticRecordNameFlyweight = new StaticRecordName(value, type);
    }

    // Anonymous name
    private StaticName()
    {
        super();
        this.value = "";
        this.type = Type.ANONYMOUS;
        this.staticRecordNameFlyweight = new StaticRecordName(value, type);
    }

    private final static StaticName anonymous = new StaticName();
    
    public static StaticName Anonymous()
    {
        return anonymous;
    }
    
    /*
    public Type type()
    {
        return type;
    }
    */

	public enum Type
    {
		STATIC_NAME,
        ANONYMOUS, // Also known as Empty
	}
    
    private final StaticRecordName staticRecordNameFlyweight;

    public StaticRecordName toRecordName()
    {
        return staticRecordNameFlyweight;
    }
    
    public String getStringName()
    {
        return switch (type)
        {
            case STATIC_NAME -> value;
            case ANONYMOUS -> "";
        };
    }
    
    @Override
	public String toString()
    {
        return switch (type)
        {
            case STATIC_NAME -> value;
            case ANONYMOUS -> "_";
        };
    }

    @Override
    public boolean equals(Object object)
    {
        return object instanceof StaticName staticName
            && type == staticName.type
            && value.equals(staticName.value);
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<StaticName> {

        @Override
        protected JSONObject _toJSON(StaticName object) throws JSONException {
            var output = new JSONObject();
    
            output.put("value", object.value);
            output.put("type", object.type);
            
            return output;
        }
    
    }
    
    public static class Builder extends FromJSONBuilderBase<StaticName> {
    
        @Override
        protected StaticName _fromJSON(JSONObject jsonObject) throws JSONException {
            return Type.valueOf(jsonObject.getString("type")) == Type.ANONYMOUS
            ? StaticName.Anonymous()
            : new StaticName(jsonObject.getString("value"));
        }
    
    }
}
