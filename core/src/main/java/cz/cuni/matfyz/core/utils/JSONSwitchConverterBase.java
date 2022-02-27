package cz.cuni.matfyz.core.utils;

import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public abstract class JSONSwitchConverterBase<Type extends JSONConvertible> implements JSONConverter<Type> {

    protected String name() {
        return this.getClass().getDeclaringClass().getName();
    }

    protected abstract Set<JSONConverterBase<? extends Type>> getChildConverters();

    public JSONObject toJSON(Type object) {
        return object.toJSON();
    }

    public Type fromJSON(JSONObject jsonObject) {
        for (var converter : getChildConverters()) {
            Type output = converter.fromJSON(jsonObject, true);
            if (output != null)
                return output;
        }

        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.error("From JSON failed for switch class " + name() + ".");

        return null;
    }

}
