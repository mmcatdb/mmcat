package cz.matfyz.server.entity.datainput;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class DataInputUpdate {

    public final String label;
    public final ObjectNode settings;

    @JsonCreator
    public DataInputUpdate(
        @JsonProperty("label") String label,
        @JsonProperty("settings") ObjectNode settings
    ) {
        this.label = label;
        this.settings = settings;
    }

    public boolean hasPassword() {
        return this.settings.has(DataInputEntity.PASSWORD_FIELD_NAME);
    }

    public void setPasswordFrom(DataInputEntity dataInput) {
        this.settings.set(DataInputEntity.PASSWORD_FIELD_NAME, dataInput.settings.get(DataInputEntity.PASSWORD_FIELD_NAME));
    }

}