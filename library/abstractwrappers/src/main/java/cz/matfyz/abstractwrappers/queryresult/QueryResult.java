package cz.matfyz.abstractwrappers.queryresult;

/**
 * This class represents the result of a query. It contains (a) data and (b) some statistics about the query.
 * Because we need to support all possible database systems, the data is represented as a tree.
 */
public class QueryResult {

    public final ResultNode data;
    public final Object statistics;

    public QueryResult(ResultNode data, Object statistics) {
        this.data = data;
        this.statistics = statistics;
    }

}
