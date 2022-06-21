package cz.cuni.matfyz.server.entity.database;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 
 * @author jachym.bartik
 */
public class DatabaseUpdate {

    public String label;
    public ObjectNode settings;

    public boolean hasPassword() {
        return this.settings.has(Database.PASSWORD_FIELD_NAME);
    }

    public void setPasswordFrom(Database database) {
        this.settings.set(Database.PASSWORD_FIELD_NAME, database.settings.get(Database.PASSWORD_FIELD_NAME));
    }

}