package cz.cuni.matfyz.core.serialization;

import org.json.*;

/**
 *
 * @author jachymb.bartik
 */
public interface ToJSONConverter<Type extends JSONConvertible>
{
    public JSONObject toJSON(Type object);
}
