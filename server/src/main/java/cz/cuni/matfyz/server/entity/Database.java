package cz.cuni.matfyz.server.entity;

import org.json.JSONObject;

/**
 * 
 * @author jachym.bartik
 */
public class Database extends Entity {

    public final Type type;
    public final String label;

    private Database(Integer id, Type type, String label) {
        super(id);
        this.type = type;
        this.label = label;
    }

    // TODO
    public static Database fromJSON(Integer id, String jsonValue) {
        try {
            var json = new JSONObject(jsonValue);

            Type type = Type.valueOf(json.getString("type"));
            String label = json.getString("label");

            return new Database(id, type, label);
        }
        catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }

    public enum Type {
        mongodb,
        postgresql
    }
    
}