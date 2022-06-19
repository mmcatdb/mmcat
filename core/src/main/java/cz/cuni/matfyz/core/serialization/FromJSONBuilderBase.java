package cz.cuni.matfyz.core.serialization;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public abstract class FromJSONBuilderBase<Type extends JSONConvertible> implements FromJSONBuilder<Type> {

    /**
     * A default name-of-the-Type-class implementation which expects the JSONConverter<Type> class to be an inner class of the Type.
     */
    protected String name() {
        return this.getClass().getDeclaringClass().getSimpleName();
    }

    public Type fromJSON(JSONObject jsonObject) {
        try {
            return _fromJSON(jsonObject);
        }
        catch (JSONException exception) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("From JSON failed for " + name() + ".", exception);
        }

        return null;
    }

    protected abstract Type _fromJSON(JSONObject jsonObject) throws JSONException;

    public Type fromJSON(String jsonValue) {
        try {
            JSONObject jsonObject = new JSONObject(jsonValue);
            return fromJSON(jsonObject);
        }
        catch (JSONException exception) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("From JSON failed for " + name() + " because of invalid input string.", exception);
        }

        return null;
    }

}
