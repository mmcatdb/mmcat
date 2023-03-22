package cz.cuni.matfyz.server.entity.database;

import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.repository.utils.Utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author jachym.bartik
 */
public class Database extends Entity {
    
    public static final String PASSWORD_FIELD_NAME = "password";

    public Type type;
    public String label;
    public ObjectNode settings;

    public enum Type {
        mongodb,
        postgresql,
        neo4j
    }

    @JsonCreator
    public Database(@JsonProperty("id") Id id) {
        super(id);
    }

    public Database(Id id, DatabaseInit data) {
        super(id);
        this.type = data.type;
        this.label = data.label;
        this.settings = data.settings;
    }

    public Database(Id id, Database database) {
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

    private static final ObjectReader dataJsonReader = new ObjectMapper().readerFor(DatabaseInit.class);

    public static Database fromJsonValue(Id id, String jsonValue) throws JsonProcessingException {
        DatabaseInit data = dataJsonReader.readValue(jsonValue);
        return new Database(id, data);
    }

    private DatabaseInit toDatabaseInit() {
        return new DatabaseInit(label, settings, type);
    }

    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJsonWithoutProperties(this, "id");
    }

    public DatabaseInfo toInfo() {
        return new DatabaseInfo(id, type, label);
    }
    
}