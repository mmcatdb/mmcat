package cz.matfyz.querying.exception;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.querying.core.querytree.QueryNode;

import java.io.Serializable;

public class QueryTreeException extends QueryingException {

    protected QueryTreeException(String name, Serializable data) {
        super("queryTree." + name, data, null);
    }

    public static QueryTreeException nodeNotRoot(QueryNode node) {
        return new QueryTreeException("nodeNotRoot", node.getClass().getSimpleName());
    }

    private record TwoDatasources(
        String datasource1,
        String datasource2
    ) implements Serializable {}

    public static QueryTreeException multipleDatasources(Datasource datasource1, Datasource datasource2) {
        return new QueryTreeException("multipleDatasources", new TwoDatasources(datasource1.identifier, datasource2.identifier));
    }

    public static QueryTreeException unsupportedOutsideDatasource(QueryNode node) {
        return new QueryTreeException("unsupportedOutsideDatasource", node.getClass().getSimpleName());
    }

    public static QueryTreeException unsupportedInsideDatasource(QueryNode node) {
        return new QueryTreeException("unsupportedInsideDatasource", node.getClass().getSimpleName());
    }

}
