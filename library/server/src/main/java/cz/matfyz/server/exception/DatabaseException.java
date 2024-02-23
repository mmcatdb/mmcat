package cz.matfyz.server.exception;

import cz.matfyz.abstractwrappers.database.Database.DatabaseType;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.database.DatabaseEntity;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public class DatabaseException extends ServerException {

    private record Data(
        Id databseId,
        DatabaseType type
    ) implements Serializable {}

    private DatabaseException(String name, DatabaseEntity database, Throwable cause) {
        super("database." + name, new Data(database.id, database.type), cause);
    }

    public static DatabaseException wrapperNotFound(DatabaseEntity database) {
        return new DatabaseException("wrapperNotFound", database, null);
    }

    public static DatabaseException wrapperNotCreated(DatabaseEntity database, Throwable cause) {
        return new DatabaseException("wrapperNotCreated", database, cause);
    }

}
