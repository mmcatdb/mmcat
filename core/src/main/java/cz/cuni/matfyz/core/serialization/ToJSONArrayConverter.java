package cz.cuni.matfyz.core.serialization;

import org.json.JSONArray;

/**
 * @author jachymb.bartik
 */
public interface ToJSONArrayConverter<T extends JSONArrayConvertible> {

    public JSONArray toJSON(T object);
    
}
