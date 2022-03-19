package cz.cuni.matfyz.server.entity;

import org.json.JSONObject;

/**
 * 
 * @author jachym.bartik
 */
public class SchemaCategoryInfo extends Entity {

    public final String jsonValue;

    public SchemaCategoryInfo(Integer id, String jsonValue) {
        super(id);
        this.jsonValue = jsonValue;
    }

}
