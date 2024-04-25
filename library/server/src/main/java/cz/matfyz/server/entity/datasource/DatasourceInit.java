package cz.matfyz.server.entity.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cz.matfyz.abstractwrappers.datasource.Datasource.DatasourceType;

public class DatasourceInit extends DatasourceUpdate {

    public final DatasourceType type;

    @JsonCreator
    public DatasourceInit(
        @JsonProperty("label") String label,
        @JsonProperty("type") DatasourceType type,
        @JsonProperty("settings") ObjectNode settings
    ) {
        super(label, settings);
        this.type = type;
    }

}