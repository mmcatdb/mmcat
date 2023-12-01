package cz.matfyz.server.entity.database;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author jachym.bartik
 */
public class DatabaseUpdate {

    public final String label;
    public final ObjectNode settings;

    @JsonCreator
    public DatabaseUpdate(
        @JsonProperty("label") String label,
        @JsonProperty("settings") ObjectNode settings
    ) {
        this.label = label;
        this.settings = settings;
    }

    public boolean hasPassword() {
        return this.settings.has(DatabaseEntity.PASSWORD_FIELD_NAME);
    }

    public void setPasswordFrom(DatabaseEntity database) {
        this.settings.set(DatabaseEntity.PASSWORD_FIELD_NAME, database.settings.get(DatabaseEntity.PASSWORD_FIELD_NAME));
    }

}