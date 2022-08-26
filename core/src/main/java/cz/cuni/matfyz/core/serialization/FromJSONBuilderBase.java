package cz.cuni.matfyz.core.serialization;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public abstract class FromJSONBuilderBase<T extends JSONConvertible> implements FromJSONBuilder<T> {

    /**
     * A default name-of-the-T-class implementation which expects the JSONConverter-T class to be an inner class of the T.
     */
    protected String name() {
        return this.getClass().getDeclaringClass().getSimpleName();
    }

    protected abstract T innerFromJSON(JSONObject jsonObject) throws JSONException;

    public T fromJSON(JSONObject jsonObject) {
        try {
            return innerFromJSON(jsonObject);
        }
        catch (JSONException exception) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("From JSON failed for " + name() + ".", exception);
        }

        return null;
    }

    public T fromJSON(String jsonValue) {
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
