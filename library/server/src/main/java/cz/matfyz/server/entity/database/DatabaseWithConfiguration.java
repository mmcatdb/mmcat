package cz.matfyz.server.entity.database;

import cz.matfyz.abstractwrappers.database.Database.DatabaseType;
import cz.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public class DatabaseWithConfiguration {

    public final Id id;
    public final DatabaseType type;
    public final String label;
    public final DatabaseConfiguration configuration;

    public DatabaseWithConfiguration(Database database, DatabaseConfiguration configuration) {
        this.id = database.id;
        this.type = database.type;
        this.label = database.label;
        this.configuration = configuration;
    }

}
