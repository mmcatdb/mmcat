package cz.matfyz.server.entity.database;

import cz.matfyz.abstractwrappers.database.Database.DatabaseType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author jachym.bartik
 */
public class DatabaseInit extends DatabaseUpdate {

    public final DatabaseType type;

    @JsonCreator
    public DatabaseInit(
        @JsonProperty("label") String label,
        @JsonProperty("settings") ObjectNode settings,
        @JsonProperty("type") DatabaseType type
    ) {
        super(label, settings);
        this.type = type;
    }

}
