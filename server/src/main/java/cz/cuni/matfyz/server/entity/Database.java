package cz.cuni.matfyz.server.entity;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jachym.bartik
 */
public class Database extends Entity {

    private static Logger LOGGER = LoggerFactory.getLogger(Database.class);

    public final Type type;
    public final String label;
    public final String jsonSettings;

    private Database(Integer id, Type type, String label, String jsonSettings) {
        super(id);
        this.type = type;
        this.label = label;
        this.jsonSettings = jsonSettings;
    }

    // TODO
    public static Database fromJSON(Integer id, String jsonValue) {
        try {
            var json = new JSONObject(jsonValue);

            Type type = Type.valueOf(json.getString("type"));
            String label = json.getString("label");
            String jsonSettings = json.getJSONObject("settings").toString();

            return new Database(id, type, label, jsonSettings);
        }
        catch (Exception exception) {
            LOGGER.error("Database with id " + id + " and jsonValue:\n" + jsonValue + "\ncannot be created from JSON.", exception);
        }

        return null;
    }

    public enum Type {
        mongodb,
        postgresql
    }
    
}