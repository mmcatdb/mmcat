package cz.cuni.matfyz.core.serialization;

import org.json.*;

/**
 *
 * @author jachymb.bartik
 */
public interface FromJSONBuilder<Type extends JSONConvertible>
{
    public Type fromJSON(JSONObject jsonObject);
}
