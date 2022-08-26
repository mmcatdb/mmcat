package cz.cuni.matfyz.core.serialization;

import org.json.JSONObject;

/**
 * @author jachymb.bartik
 */
public interface ToJSONConverter<T extends JSONConvertible> {

    public JSONObject toJSON(T object);
    
}
