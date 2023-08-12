package cz.matfyz.server.exception;

/**
 * @author jachymb.bartik
 */
public class RepositoryException extends ServerException {

    private RepositoryException(String name, Throwable cause) {
        super("repository." + name, null, cause);
    }

    public static RepositoryException createConnection(Throwable cause) {
        return new RepositoryException("createConnection", cause);
    }

    public static RepositoryException executeSql(Throwable cause) {
        return new RepositoryException("executeSql", cause);
    }

    public static RepositoryException processJson(Throwable cause) {
        return new RepositoryException("processJson", cause);
    }

}