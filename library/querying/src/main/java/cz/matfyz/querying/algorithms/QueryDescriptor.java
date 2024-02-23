package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.querying.algorithms.translator.QueryTranslator;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.QueryDescription;
import cz.matfyz.querying.core.QueryDescription.QueryPartDescription;
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

import java.util.List;
import java.util.stream.Stream;

/**
 * This class translates a query tree to a query for a specific database.
 * The provided tree has to have `database`, meaning it can be fully resolved withing the given database system.
 */
public class QueryDescriptor implements QueryVisitor<QueryDescription> {

    public static  QueryDescription run(QueryContext context, QueryNode rootNode) {
        return new QueryDescriptor(context, rootNode).run();
    }

    private final QueryContext context;
    private final QueryNode rootNode;

    private QueryDescriptor(QueryContext context, QueryNode rootNode) {
        this.context = context;
        this.rootNode = rootNode;
    }

    private QueryDescription run() {
        return rootNode.accept(this);
    }

    public QueryDescription visit(DatabaseNode node) {
        final var translator = new QueryTranslator(context, node);
        final QueryStatement query = translator.run();

        return new QueryDescription(List.of(new QueryPartDescription(node.database.identifier, query)));
    }

    public QueryDescription visit(PatternNode node) {
        throw QueryTreeException.unsupportedOutsideDatabase(node);
    }

    public QueryDescription visit(FilterNode node) {
        return node.child.accept(this);
    }

    public QueryDescription visit(JoinNode node) {
        return concatenate(node.fromChild.accept(this), node.toChild.accept(this));
    }

    public QueryDescription visit(MinusNode node) {
        return concatenate(node.primaryChild.accept(this), node.minusChild.accept(this));
    }

    public QueryDescription visit(OptionalNode node) {
        return concatenate(node.primaryChild.accept(this), node.optionalChild.accept(this));
    }

    public QueryDescription visit(UnionNode node) {
        return concatenate(node.children.stream().map(child -> child.accept(this)).toArray(QueryDescription[]::new));
    }

    private QueryDescription concatenate(QueryDescription... descriptions) {
        final var parts = Stream.of(descriptions).flatMap(description -> description.parts().stream()).toList();

        return new QueryDescription(parts);
    }

}
