package cz.cuni.matfyz.server.entity.database;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 
 * @author jachym.bartik
 */
public class DatabaseInit extends DatabaseUpdate {

    public Database.Type type;

    public DatabaseInit() {}
    
    public DatabaseInit(String label, ObjectNode settings, Database.Type type) {
        this.label = label;
        this.settings = settings;
        this.type = type;
    }
    
}