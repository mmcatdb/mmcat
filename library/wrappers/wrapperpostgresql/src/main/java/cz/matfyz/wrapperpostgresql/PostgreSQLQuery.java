package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.querycontent.QueryContent;

import java.util.List;

/**
 * Represents a PostgreSQL query with "type-safe" parameters to be used with PostgreSQL's prepareStatement().
 */
public class PostgreSQLQuery implements QueryContent {

    public final String queryString;
    /** Values that should be escaped when added to the prepared query statement. */
    public final List<String> rawVariables;
    /** Neccessary for a part of PostgreSQLCollectorWrapper to work. If that specific functionality is removed, remove this as well. */
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
