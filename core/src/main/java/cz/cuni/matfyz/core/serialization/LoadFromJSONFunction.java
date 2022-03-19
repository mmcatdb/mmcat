package cz.cuni.matfyz.core.serialization;

import org.json.JSONObject;

/**
 * 
 * @author jachym.bartik
 */
interface LoadFromJSONFunction<Type extends JSONConvertible> {

    void load(Type object, JSONObject jsonObject);

}
