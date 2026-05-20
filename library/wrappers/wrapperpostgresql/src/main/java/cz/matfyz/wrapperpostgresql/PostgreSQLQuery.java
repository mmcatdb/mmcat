package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.querycontent.QueryContent;

import java.util.List;

/**
 * Represents a PostgreSQL query. tableColumns Annotation is neccessary for PostgreSQLCollectorWrapper to work. A potential future improvement could annotate the query more thoroughly, or even make it fully constructable from the annotations.
 */
public class PostgreSQLQuery implements QueryContent {

    public final String queryString;
    /** Values that should be escaped when added to the prepared query statement. */
    public final List<String> rawVariables;
    public final List<String> tableColumns;

    public PostgreSQLQuery(String queryString, List<String> rawVariables, List<String> tableColumns) {
        this.queryString = queryString;
        this.rawVariables = rawVariables;
        this.tableColumns = tableColumns;
    }

    @Override public String toString() {
        return queryString;
    }

}
