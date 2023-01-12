package cz.cuni.matfyz.core.serialization;

import org.json.JSONArray;

/**
 * @author jachymb.bartik
 */
public interface FromJSONArrayBuilder<T extends JSONArrayConvertible> {

    public T fromJSON(JSONArray jsonArray);

    public T fromJSON(String jsonValue);

}
