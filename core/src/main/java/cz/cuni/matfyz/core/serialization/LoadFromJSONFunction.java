package cz.cuni.matfyz.core.serialization;

import org.json.JSONObject;

/**
 * @author jachym.bartik
 */
interface LoadFromJSONFunction<T extends JSONConvertible> {

    void load(T object, JSONObject jsonObject);

}
