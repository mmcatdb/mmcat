package cz.matfyz.server.entity.datainput;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cz.matfyz.abstractwrappers.database.Database.DatabaseType;
import cz.matfyz.server.entity.database.DatabaseUpdate;
import cz.matfyz.server.entity.datasource.DataSource;

public class DataInputInit extends DatabaseUpdate {

    public final DatabaseType databaseType;
    public final DataSource.Type dataSourceType;

    @JsonCreator
    public DataInputInit(
        @JsonProperty("label") String label,
        @JsonProperty("databaseType") DatabaseType databaseType,
        @JsonProperty("dataSourceType") DataSource.Type dataSourceType,
        @JsonProperty("settings") ObjectNode settings
    ) {
        super(label, settings);
        this.databaseType = databaseType;
        this.dataSourceType = dataSourceType;
    }

}