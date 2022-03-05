package cz.cuni.matfyz.core.serialization;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public abstract class ToJSONConverterBase<Type extends JSONConvertible> implements ToJSONConverter<Type> {

    /**
     * A default name-of-the-Type-class implementation which expects the JSONConverter<Type> class to be an inner class of the Type.
     */
    protected String name() {
        return this.getClass().getDeclaringClass().getSimpleName();
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

}
