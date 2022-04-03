package cz.cuni.matfyz.server.view;

import java.io.Serializable;

import cz.cuni.matfyz.server.entity.Database;

/**
 * 
 * @author jachym.bartik
 */
public class DatabaseView implements Serializable {

    public int id;
    public String type;
    public String label;
    public DatabaseConfiguration configuration;

    public DatabaseView(Database database) {
        this.id = database.id;
        this.type = database.type;
        this.label = database.label;
        this.configuration = new DatabaseConfiguration(database.getPathWrapper());
    }

}
