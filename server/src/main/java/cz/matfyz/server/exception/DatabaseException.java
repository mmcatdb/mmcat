package cz.matfyz.server.exception;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.database.Database;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public class DatabaseException extends ServerException {

    private record Data(
        Id databseId,
        Database.Type type
    ) implements Serializable {}

    private DatabaseException(String name, Database database, Throwable cause) {
        super("database." + name, new Data(database.id, database.type), cause);
    }

    public static DatabaseException wrapperNotFound(Database database) {
        return new DatabaseException("wrapperNotFound", database, null);
    }

    public static DatabaseException wrapperNotCreated(Database database, Throwable cause) {
        return new DatabaseException("wrapperNotCreated", database, cause);
    }

}