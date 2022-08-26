package cz.cuni.matfyz.core.serialization;

import org.json.JSONObject;

/**
 * @author jachymb.bartik
 */
public interface FromJSONBuilder<T extends JSONConvertible> {

    public T fromJSON(JSONObject jsonObject);

    public T fromJSON(String jsonValue);

}
