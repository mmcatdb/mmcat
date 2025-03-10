package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.BaseControlWrapper.ControlWrapperProvider;
import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.querying.queryresult.ResultList;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.core.QueryDescription;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.parsing.Query;
import cz.matfyz.querying.parsing.QueryParser;

import java.util.List;

/**
 * Given a MMQL `queryString`, execute this query against the given `schemaCategory`.
 * Returns an instance category with the results of the query.
 */
public class QueryToInstance {

    private final ControlWrapperProvider provider;
    private final SchemaCategory schema;
    private final String queryString;
    private final List<Mapping> kinds;

    public QueryToInstance(ControlWrapperProvider provider, SchemaCategory schema, String queryString, List<Mapping> kinds) {
        this.provider = provider;
        this.schema = schema;
        this.queryString = queryString;
        this.kinds = kinds;
    }

    public ResultList execute() {
        try {
            return innerExecute();
        }
        catch (NamedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new OtherException(e);
        }
    }

    private ResultList innerExecute() {
        final Query query = QueryParser.parse(queryString);
        query.context.setProvider(provider);
        final QueryNode queryTree = QueryTreeBuilder.run(query.context, schema, kinds, query.where);
        final QueryResult selection = QueryResolver.run(query.context, queryTree);
        final QueryResult projection = QueryProjector.run(query.context, query.select, selection);

        return projection.data;
    }

    public QueryDescription describe() {
        try {
            return innerDescribe();
        }
        catch (NamedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new OtherException(e);
        }
    }

    private QueryDescription innerDescribe() {
        final Query query = QueryParser.parse(queryString);
        query.context.setProvider(provider);
        final QueryNode queryTree = QueryTreeBuilder.run(query.context, schema, kinds, query.where);

        return QueryDescriptor.run(query.context, queryTree);
    }

}
