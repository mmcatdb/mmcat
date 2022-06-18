package cz.cuni.matfyz.server.entity.database;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 
 * @author jachym.bartik
 */
public class CreationData extends UpdateData {

    public Database.Type type;

    public CreationData() {}
    
    public CreationData(String label, ObjectNode settings, Database.Type type) {
        this.label = label;
        this.settings = settings;
        this.type = type;
    }
    
}