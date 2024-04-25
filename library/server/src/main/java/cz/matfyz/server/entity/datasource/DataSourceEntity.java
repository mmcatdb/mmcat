package cz.matfyz.server.entity.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cz.matfyz.abstractwrappers.datasource.DataSource.DataSourceType;
import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.utils.Utils;

public class DataSourceEntity extends Entity {

    public static final String PASSWORD_FIELD_NAME = "password";

    public String label;
    
    public DataSourceType type;    
    
    public ObjectNode settings;
    

    @JsonCreator
    public DataSourceEntity(@JsonProperty("id") Id id) {
        super(id);
    }

    public DataSourceEntity(Id id, DataSourceInit data) {
        super(id);
        this.label = data.label;
        this.type = data.type;
        this.settings = data.settings;
    }

    public DataSourceEntity(Id id, DataSourceEntity dataSource) {
        this(id, dataSource.toDataSourceInit());
    }

 /*   public boolean isDB() {
        if (this.type == DataSourceType.mongodb || this.type == DataSourceType.neo4j ||
                this.type == DataSourceType.postgresql) {
            return true;
        }
        return false;
    }*/
    
    
    public void hidePassword() {
        this.settings.remove(PASSWORD_FIELD_NAME);
    }

    public void updateFrom(DataSourceUpdate data) {
        if (data.label != null)
            this.label = data.label;

        if (data.settings != null)
            this.settings = data.settings;
    }

    private static final ObjectReader dataJsonReader = new ObjectMapper().readerFor(DataSourceInit.class);

    public static DataSourceEntity fromJsonValue(Id id, String jsonValue) throws JsonProcessingException {
        DataSourceInit data = dataJsonReader.readValue(jsonValue);
        return new DataSourceEntity(id, data);
    }

    public DataSourceInit toDataSourceInit() {
        return new DataSourceInit(label, type, settings);
    }

    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJsonWithoutProperties(this, "id");
    }

    public DataSourceInfo toInfo() {
        return new DataSourceInfo(id, type, label);
    }


}
