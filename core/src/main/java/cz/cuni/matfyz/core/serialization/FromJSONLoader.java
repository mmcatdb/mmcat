package cz.cuni.matfyz.core.serialization;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public abstract class FromJSONLoader<Type extends JSONConvertible> {

    /**
     * A default name-of-the-Type-class implementation which expects the JSONConverter<Type> class to be an inner class of the Type.
     */
    protected String name() {
        return "TODO";
        //return this.getClass().getDeclaringClass().getSimpleName();
    }

    private final LoadFromJSONFunction<Type> loadFunction;

    public FromJSONLoader(LoadFromJSONFunction<Type> loadFunction) {
        this.loadFunction = loadFunction;
    }

    public void loadFromJSON(Type object, JSONObject jsonObject) {
        _loadFromJSON(object, jsonObject);
    }

    public void loadFromJSON(Type object, String jsonValue) {
        try {
            JSONObject jsonObject = new JSONObject(jsonValue);
            _loadFromJSON(object, jsonObject);
        }
        catch (JSONException exception) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("From JSON failed for " + name() + " because invalid input string.", exception);
        }
    }

    private void _loadFromJSON(Type object, JSONObject jsonObject) {
        try {
            String className = jsonObject.getString("_class");
            if (className.equals(name()))
                loadFunction.load(object, jsonObject);
            else {
                Logger logger = LoggerFactory.getLogger(this.getClass());
                logger.error("From JSON failed for " + name() + " because of mismatch in \"_class\": \"" + className + "\".");
            }

        }
        catch (JSONException exception) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("Load from JSON failed for " + name() + ".", exception);
        }
    }

}
