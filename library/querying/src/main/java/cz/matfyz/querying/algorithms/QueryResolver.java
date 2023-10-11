package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.queryresult.QueryResult;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.querytree.DatabaseNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.PatternNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.exception.QueryTreeException;
import cz.matfyz.querying.parsing.ConditionFilter;
import cz.matfyz.querying.parsing.ValueFilter;

/**
 * This class translates a query tree to a query for a specific database.
 * The provided tree has to have `database`, meaning it can be fully resolved withing the given database system.
 */
public class QueryResolver implements QueryVisitor<QueryResult> {

    public static QueryResult run(QueryContext context, QueryNode rootNode) {
        return new QueryResolver(context, rootNode).run();
    }

    private final QueryContext context;
    private final QueryNode rootNode;

    private QueryResolver(QueryContext context, QueryNode rootNode) {
        this.context = context;
        this.rootNode = rootNode;
    }

    private QueryResult run() {
        return rootNode.accept(this);
    }

    public QueryResult visit(DatabaseNode node) {
        final var translator = new QueryTranslator(context, node);
        final QueryStatement query = translator.run();
        final var pullWrapper = node.database.control.getPullWrapper();

        return pullWrapper.executeQuery(query);
    }

    public QueryResult visit(PatternNode node) {
        throw QueryTreeException.unsupportedOutsideDatabase(node);
    }

    public QueryResult visit(FilterNode node) {
        if (node.filter instanceof ConditionFilter conditionFilter) {
            // TODO
            throw new UnsupportedOperationException();
        }
        else if (node.filter instanceof ValueFilter valueFilter) {
            // TODO
            throw new UnsupportedOperationException();
        }

        throw new UnsupportedOperationException();
    }

    public QueryResult visit(JoinNode node) {
        throw new UnsupportedOperationException();
    }

    public QueryResult visit(MinusNode node) {
        throw new UnsupportedOperationException();
    }

    public QueryResult visit(OptionalNode node) {
        throw new UnsupportedOperationException();
    }

    public QueryResult visit(UnionNode node) {
        throw new UnsupportedOperationException();
    }

}