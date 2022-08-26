package cz.cuni.matfyz.server.entity.database;

/**
 * @author jachym.bartik
 */
public class DatabaseView {

    public final int id;
    public final Database.Type type;
    public final String label;
    public final DatabaseConfiguration configuration;

    public DatabaseView(Database database, DatabaseConfiguration configuration) {
        this.id = database.id;
        this.type = database.type;
        this.label = database.label;
        this.configuration = configuration;
    }

}
