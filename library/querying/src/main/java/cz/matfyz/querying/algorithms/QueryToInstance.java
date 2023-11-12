package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.abstractwrappers.queryresult.ResultList;
import cz.matfyz.abstractwrappers.queryresult.QueryResult;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.parsing.Query;
import cz.matfyz.querying.parsing.QueryParser;

import java.util.List;

/**
 * Given a MMQL `queryString`, execute this query against the given `schemaCategory`.
 * Returns an instance category with the results of the query.
 */
public class QueryToInstance {

    private String queryString;
    private SchemaCategory schema;
    private List<Kind> kinds;

    public void input(SchemaCategory category, String queryString, List<Kind> kinds) {
        this.schema = category;
        this.queryString = queryString;
        this.kinds = kinds;
    }

    public ResultList algorithm() {
        final Query query = QueryParser.run(queryString);
        final QueryNode queryTree = QueryTreeBuilder.run(query.context, schema, kinds, query.where);
        final QueryResult selection = QueryResolver.run(query.context, queryTree);
        final QueryResult projection = QueryProjector.run(query.context, query.select, selection);

        return projection.data;
    }
    
}
