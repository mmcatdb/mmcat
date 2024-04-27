package cz.matfyz.core.querying.queryresult;

import cz.matfyz.core.querying.QueryStructure;

/**
 * This class represents the result of a selection part of a query. It contains (a) data and (b) structure of the data.
 * Because we need to support all possible datasources, the data is represented as a tree.
 */
public class QueryResult {

    public final ResultList data;
    public final QueryStructure structure;

    public QueryResult(ResultList data, QueryStructure structure) {
        this.data = data;
        this.structure = structure;
    }

}
