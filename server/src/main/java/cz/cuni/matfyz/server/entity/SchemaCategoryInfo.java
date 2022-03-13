package cz.cuni.matfyz.server.entity;

import org.json.JSONObject;

/**
 * 
 * @author jachym.bartik
 */
public class SchemaCategoryInfo
{
    public final int id;
    public final String jsonValue;

    public SchemaCategoryInfo(int id, String jsonValue)
    {
        this.id = id;
        this.jsonValue = jsonValue;
    }
}
