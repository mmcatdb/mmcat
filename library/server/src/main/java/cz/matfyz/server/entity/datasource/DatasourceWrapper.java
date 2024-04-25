package cz.matfyz.server.entity.datasource;

import cz.matfyz.abstractwrappers.datasource.Datasource.DatasourceType;
import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.utils.Utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DatasourceWrapper extends Entity {

    public static final String PASSWORD_FIELD_NAME = "password";

    public String label;
    public DatasourceType type;    
    public ObjectNode settings;

    @JsonCreator
    public DatasourceWrapper(@JsonProperty("id") Id id) {
        super(id);
    }

    public DatasourceWrapper(Id id, DatasourceInit data) {
        super(id);
        this.label = data.label;
        this.type = data.type;
        this.settings = data.settings;
    }

    public DatasourceWrapper(Id id, DatasourceWrapper datasource) {
        this(id, datasource.toDatasourceInit());
    }
    
    public void hidePassword() {
        this.settings.remove(PASSWORD_FIELD_NAME);
    }

    public void updateFrom(DatasourceUpdate data) {
        if (data.label != null)
            this.label = data.label;

        if (data.settings != null)
            this.settings = data.settings;
    }

    private static final ObjectReader dataJsonReader = new ObjectMapper().readerFor(DatasourceInit.class);

    public static DatasourceWrapper fromJsonValue(Id id, String jsonValue) throws JsonProcessingException {
        DatasourceInit data = dataJsonReader.readValue(jsonValue);
        return new DatasourceWrapper(id, data);
    }

    public DatasourceInit toDatasourceInit() {
        return new DatasourceInit(label, type, settings);
    }

    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJsonWithoutProperties(this, "id");
    }

    public DatasourceInfo toInfo() {
        return new DatasourceInfo(id, type, label);
    }

}
