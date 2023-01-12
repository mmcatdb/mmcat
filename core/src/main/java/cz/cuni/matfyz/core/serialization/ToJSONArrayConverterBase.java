package cz.cuni.matfyz.core.serialization;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public abstract class ToJSONArrayConverterBase<T extends JSONArrayConvertible> implements ToJSONArrayConverter<T> {

    /**
     * A default name-of-the-Type-class implementation which expects the JSONConverter-T class to be an inner class of the Type.
     */
    protected String name() {
        return this.getClass().getDeclaringClass().getSimpleName();
    }

    @Override
    public JSONArray toJSON(T object) {
        try {
            return innerToJSON(object);
        }
        catch (JSONException exception) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("To JSON failed for " + name() + ".", exception);
        }

        return null;
    }

    protected abstract JSONArray innerToJSON(T object) throws JSONException;

}
