package cz.cuni.matfyz.core.serialization;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public abstract class FromJSONArrayBuilderBase<T extends JSONArrayConvertible> implements FromJSONArrayBuilder<T> {

    /**
     * A default name-of-the-T-class implementation which expects the JSONConverter-T class to be an inner class of the T.
     */
    protected String name() {
        return this.getClass().getDeclaringClass().getSimpleName();
    }

    protected abstract T innerFromJSON(JSONArray jsonArray) throws JSONException;

    public T fromJSON(JSONArray jsonArray) {
        try {
            return innerFromJSON(jsonArray);
        }
        catch (JSONException exception) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("From JSON failed for " + name() + ".", exception);
        }

        return null;
    }

    public T fromJSON(String jsonValue) {
        try {
            JSONArray jsonArray = new JSONArray(jsonValue);
            return fromJSON(jsonArray);
        }
        catch (JSONException exception) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("From JSON failed for " + name() + " because of invalid input string.", exception);
        }

        return null;
    }

}
