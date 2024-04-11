package cz.matfyz.server.entity.datainput;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cz.matfyz.abstractwrappers.database.Database.DatabaseType;
import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DataSource;
import cz.matfyz.server.repository.utils.Utils;

public class DataInput extends Entity {

    public static final String PASSWORD_FIELD_NAME = "password";

    public String label;
    
    public DatabaseType databaseType;    // NEED TO UNITE THESE!!!
    public DataSource.Type dataSourceType;
    
    public ObjectNode settings;
    
    public boolean isDB = false;

    @JsonCreator
    public DataInput(@JsonProperty("id") Id id) {
        super(id);
    }

    public DataInput(Id id, DataInputInit data) {
        super(id);
        this.label = data.label;
        this.databaseType = data.databaseType;
        this.dataSourceType = data.dataSourceType;
        this.settings = data.settings;
        setIsDB();
    }

    public DataInput(Id id, DataInput dataInput) {
        this(id, dataInput.toDataInputInit());
    }

    public void setIsDB() {
        this.isDB = databaseType != null;
    }
    
    public boolean getIsDB() {
        return this.isDB;
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

    public static DataInput fromJsonValue(Id id, String jsonValue) throws JsonProcessingException {
        DataInputInit data = dataJsonReader.readValue(jsonValue);
        return new DataInput(id, data);
    }

    public DataInputInit toDataInputInit() {
        return new DataInputInit(label, databaseType, dataSourceType, settings);
    }

    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJsonWithoutProperties(this, "id");
    }
/*
    public DatabaseInfo toInfo() {
        return new DatabaseInfo(id, type, label);
    }*/


}
