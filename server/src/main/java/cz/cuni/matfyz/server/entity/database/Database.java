package cz.cuni.matfyz.server.entity.database;

import cz.cuni.matfyz.server.entity.Entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author jachym.bartik
 */
public class Database extends Entity {
    
    public static final String PASSWORD_FIELD_NAME = "password";
    
    private static ObjectReader dataJSONReader = new ObjectMapper().readerFor(DatabaseInit.class);
    private static ObjectWriter dataJSONWriter = new ObjectMapper().writer();

    public Type type;
    public String label;
    public ObjectNode settings;

    public enum Type {
        mongodb,
        postgresql
    }

    @JsonCreator
    public Database(@JsonProperty("id") int id) {
        super(id);
    }

    public Database(Integer id, DatabaseInit data) {
        super(id);
        this.type = data.type;
        this.label = data.label;
        this.settings = data.settings;
    }

    public Database(Integer id, Database database) {
        this(id, database.toDatabaseInit());
    }

    public void hidePassword() {
        this.settings.remove(PASSWORD_FIELD_NAME);
    }

    public void updateFrom(DatabaseUpdate data) {
        if (data.label != null)
            this.label = data.label;
        
        if (data.settings != null)
            this.settings = data.settings;
    }

    public static Database fromJSONValue(Integer id, String jsonValue) throws JsonProcessingException {
        DatabaseInit data = dataJSONReader.readValue(jsonValue);
        return new Database(id, data);
    }

    private DatabaseInit toDatabaseInit() {
        return new DatabaseInit(label, settings, type);
    }

    public String toJSONValue() throws JsonProcessingException {
        return dataJSONWriter.writeValueAsString(this.toDatabaseInit());
    }

    public DatabaseView toView() {
        return new DatabaseView(id, type, label);
    }

    /*
    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<Database> {

        @Override
        protected JSONObject _toJSON(Database database) throws JSONException {
            var output = new JSONObject();

            output.put("type", database.type.toString());
            output.put("label", database.label);
            output.put("jsonSettings", new JSONObject(database.jsonSettings));

            return output;
        }

    }

    public static class Builder extends FromJSONLoaderBase<Database> {

        public Database fromJSON(int id, String jsonValue) {
            var database = new Database(id);
            loadFromJSON(database, jsonValue);
            return database;
        }

        @Override
        protected void _loadFromJSON(Database database, JSONObject jsonObject) throws JSONException {
            database.type = Type.valueOf(jsonObject.getString("type"));
            database.label = jsonObject.getString("label");
            database.jsonSettings = jsonObject.getJSONObject("settings").toString();
        }

        public Database fromCreate(Integer id, Create create) {
            var database = new Database(id);

            database.type = create.type;
            database.label = create.label;
            database.jsonSettings = create.jsonSettings;

            return database;
        }

    }
    */
    
}