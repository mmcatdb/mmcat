package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.querycontent.QueryContent;

import java.util.List;

/**
 * Represents a PostgreSQL query. tableColumns Annotation is neccessary for PostgreSQLCollectorWrapper to work. A potential future improvement could annotate the query more thoroughly, or even make it fully constructable from the annotations.
 */
public class PostgreSQLQuery implements QueryContent {

    public final String queryString;
    public final List<String> tableColumns;

    public PostgreSQLQuery(String queryString, List<String> tableColumns) {
        this.queryString = queryString;
        this.tableColumns = tableColumns;
    }

    /**
     * The most simple query - finds all documents in the collection.
     */
    public static PostgreSQLQuery findAll(String collection) {
        return new PostgreSQLQuery(collection, List.of());
    }

    @Override public String toString() { return queryString; }

}
