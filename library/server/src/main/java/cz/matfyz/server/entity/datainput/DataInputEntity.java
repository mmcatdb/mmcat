package cz.matfyz.server.entity.datainput;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cz.matfyz.abstractwrappers.datainput.DataInput.DataInputType;
import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.utils.Utils;

public class DataInputEntity extends Entity {

    public static final String PASSWORD_FIELD_NAME = "password";

    public String label;
    
    public DataInputType type;    
    
    public ObjectNode settings;
    

    @JsonCreator
    public DataInputEntity(@JsonProperty("id") Id id) {
        super(id);
    }

    public DataInputEntity(Id id, DataInputInit data) {
        super(id);
        this.label = data.label;
        this.type = data.type;
        this.settings = data.settings;
    }

    public DataInputEntity(Id id, DataInputEntity dataInput) {
        this(id, dataInput.toDataInputInit());
    }

    public boolean isDB() {
        if (this.type == DataInputType.mongodb || this.type == DataInputType.neo4j ||
                this.type == DataInputType.postgresql) {
            return true;
        }
        return false;
    }
    
    
    public void hidePassword() {
        this.settings.remove(PASSWORD_FIELD_NAME);
    }

    public void updateFrom(DataInputUpdate data) {
        if (data.label != null)
            this.label = data.label;

        if (data.settings != null)
            this.settings = data.settings;
    }

    private static final ObjectReader dataJsonReader = new ObjectMapper().readerFor(DataInputInit.class);

    public static DataInputEntity fromJsonValue(Id id, String jsonValue) throws JsonProcessingException {
        DataInputInit data = dataJsonReader.readValue(jsonValue);
        return new DataInputEntity(id, data);
    }

    public DataInputInit toDataInputInit() {
        return new DataInputInit(label, type, settings);
    }

    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJsonWithoutProperties(this, "id");
    }

    public DataInputInfo toInfo() {
        return new DataInputInfo(id, type, label);
    }


}
