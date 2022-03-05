package cz.cuni.matfyz.core.serialization;

import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public abstract class FromJSONSwitchBuilderBase<Type extends JSONConvertible> implements FromJSONBuilder<Type> {

    protected String name() {
        return this.getClass().getDeclaringClass().getSimpleName();
    }

    protected abstract Set<FromJSONBuilderBase<? extends Type>> getChildConverters();

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
