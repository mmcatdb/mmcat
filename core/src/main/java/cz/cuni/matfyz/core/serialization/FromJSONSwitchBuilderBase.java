package cz.cuni.matfyz.core.serialization;

import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public abstract class FromJSONSwitchBuilderBase<T extends JSONConvertible> implements FromJSONBuilder<T> {

    protected String name() {
        return this.getClass().getDeclaringClass().getSimpleName();
    }

    protected abstract Set<FromJSONBuilderBase<? extends T>> getChildConverters();

    public T fromJSON(JSONObject jsonObject) {
        try {
            for (var converter : getChildConverters()) {
                final String className = jsonObject.getString("_class");
                if (converter.name().equals(className))
                    return converter.fromJSON(jsonObject);
            }
        }
        catch (JSONException exception) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            logger.error("From JSON failed for switch class " + name() + ".", exception);
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
