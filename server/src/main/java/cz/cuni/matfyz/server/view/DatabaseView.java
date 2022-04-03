package cz.cuni.matfyz.server.view;

import cz.cuni.matfyz.server.entity.Database;

/**
 * 
 * @author jachym.bartik
 */
public class DatabaseView {

    public int id;
    public Database.Type type;
    public String label;
    public DatabaseConfiguration configuration;

    public DatabaseView(Database database, DatabaseConfiguration configuration) {
        this.id = database.id;
        this.type = database.type;
        this.label = database.label;
        this.configuration = configuration;
    }

}
