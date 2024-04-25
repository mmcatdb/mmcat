package cz.matfyz.server.entity.datasource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cz.matfyz.abstractwrappers.datasource.DataSource.DataSourceType;

public class DataSourceInit extends DataSourceUpdate {

    public final DataSourceType type;

    @JsonCreator
    public DataSourceInit(
        @JsonProperty("label") String label,
        @JsonProperty("type") DataSourceType type,
        @JsonProperty("settings") ObjectNode settings
    ) {
        super(label, settings);
        this.type = type;
    }

}