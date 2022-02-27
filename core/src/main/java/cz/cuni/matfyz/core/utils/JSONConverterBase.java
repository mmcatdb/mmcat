package cz.cuni.matfyz.core.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public abstract class JSONConverterBase<Type extends JSONConvertible> implements JSONConverter<Type> {

    /**
     * A default name-of-the-Type-class implementation which expects the JSONConverter<Type> class to be an inner class of the Type.
     */
    protected String name() {
        return this.getClass().getDeclaringClass().getName();
    }

    @Override
    public JSONObject toJSON(Type object) {
        try {
            var output = _toJSON(object);
            output.put("_class", name());
            return output;
        }
        catch (JSONException exception) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("To JSON failed for " + name() + ".", exception);
        }

        return null;
    }

    protected abstract JSONObject _toJSON(Type object) throws JSONException;

    @Override
    public Type fromJSON(JSONObject jsonObject) {
        return fromJSON(jsonObject, false);
    }

    public Type fromJSON(JSONObject jsonObject, boolean silenceClassMismatchError) {
        try {
            String className = jsonObject.getString("_class");
            if (className.equals(name()))
                return _fromJSON(jsonObject);

            if (!silenceClassMismatchError) {
                Logger logger = LoggerFactory.getLogger(this.getClass());
                logger.error("From JSON failed for " + name() + " because of mismatch in \"_class\": \"" + className + "\".");
            }

        }
        catch (JSONException exception) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("From JSON failed for " + name() + ".", exception);
        }

        return null;
    }

    protected abstract Type _fromJSON(JSONObject jsonObject) throws JSONException;

}
