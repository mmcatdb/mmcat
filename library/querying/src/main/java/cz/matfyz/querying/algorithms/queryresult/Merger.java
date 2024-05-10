package cz.matfyz.querying.algorithms.queryresult;

import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.querying.QueryStructure;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.core.JoinCandidate;

public class Merger {

    public static QueryResult run(SchemaCategory schema, QueryResult fromResult, QueryResult toResult, JoinCandidate join) {
        return new Merger(schema, fromResult, toResult).run(join);
    }

    private final SchemaCategory schema;
    private final QueryResult fromResult;
    private final QueryResult toResult;

    private Merger(SchemaCategory schema, QueryResult fromResult, QueryResult toResult) {
        this.schema = schema;
        this.fromResult = fromResult;
        this.toResult = toResult;
    }

    private QueryResult run(JoinCandidate join) {
        // There is always a single condition in the joinProperties list. Why? Or why it's a list then?
        final var condition = join.joinProperties().getFirst();

        final QueryStructure fromStructure = findStructure(fromResult, condition.from());
        final QueryStructure toStructure = findStructure(toResult, condition.to());

        throw new UnsupportedOperationException();
    }

    private QueryStructure findStructure(QueryResult result, Signature signature) {
        // TODO This might not work generally when the same schema object is used multiple times in the query. But it should work for now.
        final SchemaObject schemaObject = schema.getEdge(signature.getLast()).to();
        final QueryStructure output = GraphUtils.findDFS(result.structure, s -> s.isLeaf() && s.schemaObject.equals(schemaObject));
        if (output == null)
            // TODO this should not happen
            throw QueryException.message("QueryStructure not found for signature " + signature);

        return output;
    }

}
