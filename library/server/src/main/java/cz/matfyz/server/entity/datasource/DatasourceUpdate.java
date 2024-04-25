package cz.matfyz.server.entity.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class DatasourceUpdate {

    public final String label;
    public final ObjectNode settings;

    @JsonCreator
    public DatasourceUpdate(
        @JsonProperty("label") String label,
        @JsonProperty("settings") ObjectNode settings
    ) {
        this.label = label;
        this.settings = settings;
    }

    public boolean hasPassword() {
        return this.settings.has(DatasourceWrapper.PASSWORD_FIELD_NAME);
    }

    public void setPasswordFrom(DatasourceWrapper datasource) {
        this.settings.set(DatasourceWrapper.PASSWORD_FIELD_NAME, datasource.settings.get(DatasourceWrapper.PASSWORD_FIELD_NAME));
    }

}