package cz.matfyz.server.entity.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class DataSourceUpdate {

    public final String label;
    public final ObjectNode settings;

    @JsonCreator
    public DataSourceUpdate(
        @JsonProperty("label") String label,
        @JsonProperty("settings") ObjectNode settings
    ) {
        this.label = label;
        this.settings = settings;
    }

    public boolean hasPassword() {
        return this.settings.has(DataSourceEntity.PASSWORD_FIELD_NAME);
    }

    public void setPasswordFrom(DataSourceEntity dataSource) {
        this.settings.set(DataSourceEntity.PASSWORD_FIELD_NAME, dataSource.settings.get(DataSourceEntity.PASSWORD_FIELD_NAME));
    }

}