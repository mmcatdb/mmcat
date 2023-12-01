package cz.matfyz.server.entity.database;

import cz.matfyz.abstractwrappers.database.Database.DatabaseType;
import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.utils.Utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author jachym.bartik
 */
public class DatabaseEntity extends Entity {
    
    public static final String PASSWORD_FIELD_NAME = "password";

    public DatabaseType type;
    public String label;
    public ObjectNode settings;

    @JsonCreator
    public DatabaseEntity(@JsonProperty("id") Id id) {
        super(id);
    }

    public DatabaseEntity(Id id, DatabaseInit data) {
        super(id);
        this.type = data.type;
        this.label = data.label;
        this.settings = data.settings;
    }

    public DatabaseEntity(Id id, DatabaseEntity database) {
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

    public static DatabaseEntity fromJsonValue(Id id, String jsonValue) throws JsonProcessingException {
        DatabaseInit data = dataJsonReader.readValue(jsonValue);
        return new DatabaseEntity(id, data);
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