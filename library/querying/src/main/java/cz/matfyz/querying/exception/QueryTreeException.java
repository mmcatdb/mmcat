package cz.matfyz.querying.exception;

import cz.matfyz.abstractwrappers.database.Database;
import cz.matfyz.querying.core.querytree.QueryNode;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public class QueryTreeException extends QueryingException {

    protected QueryTreeException(String name, Serializable data) {
        super("queryTree." + name, data, null);
    }

    public static QueryTreeException nodeNotRoot(QueryNode node) {
        return new QueryTreeException("nodeNotRoot", node.getClass().getSimpleName());
    }

    private record TwoDatabases(
        String database1,
        String database2
    ) implements Serializable {}

    public static QueryTreeException multipleDatabases(Database database1, Database database2) {
        return new QueryTreeException("multipleDatabases", new TwoDatabases(database1.identifier, database2.identifier));
    }

    public static QueryTreeException unsupportedOutsideDatabase(QueryNode node) {
        return new QueryTreeException("unsupportedOutsideDatabase", node.getClass().getSimpleName());
    }

    public static QueryTreeException unsupportedInsideDatabase(QueryNode node) {
        return new QueryTreeException("unsupportedInsideDatabase", node.getClass().getSimpleName());
    }

}
