package cz.cuni.matfyz.core.serialization;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public abstract class FromJSONLoaderBase<Type extends JSONConvertible> {

    /**
     * A default name-of-the-Type-class implementation which expects the JSONConverter<Type> class to be an inner class of the Type.
     */
    protected String name() {
        return this.getClass().getDeclaringClass().getSimpleName();
    }

    protected void loadFromJSON(Type object, JSONObject jsonObject) {
        try {
            _loadFromJSON(object, jsonObject);
        }
        catch (JSONException exception) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("From JSON failed for " + name() + ".", exception);
        }
    }

    protected abstract void _loadFromJSON(Type object, JSONObject jsonObject) throws JSONException;

    protected void loadFromJSON(Type object, String jsonValue) {
        try {
            JSONObject jsonObject = new JSONObject(jsonValue);
            _loadFromJSON(object, jsonObject);
        }
        catch (JSONException exception) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("From JSON failed for " + name() + " because of invalid input string \"" + jsonValue + "\".", exception);
        }
    }

}
