package cz.matfyz.core.querying;

/**
 * This class represents the result of a selection part of a query. It contains (a) data and (b) structure of the data.
 * Because we need to support all possible datasources, the data is represented as a tree.
 */
public class QueryResult {

    public final ListResult data;
    public final ResultStructure structure;

    public QueryResult(ListResult data, ResultStructure structure) {
        this.data = data;
        this.structure = structure;
    }

}
