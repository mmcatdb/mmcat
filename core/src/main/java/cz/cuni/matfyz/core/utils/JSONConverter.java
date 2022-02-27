package cz.cuni.matfyz.core.utils;

import org.json.*;

/**
 *
 * @author jachymb.bartik
 */
public interface JSONConverter<Type extends JSONConvertible>
{
    public JSONObject toJSON(Type object);

    public Type fromJSON(JSONObject jsonObject);
}
