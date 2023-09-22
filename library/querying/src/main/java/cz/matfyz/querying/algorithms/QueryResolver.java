package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.utils.PullQuery;
import cz.matfyz.core.instance.InstanceCategory;
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
import cz.matfyz.transformations.processes.DatabaseToInstance;

/**
 * This class translates a query tree to a query for a specific database.
 * The provided tree has to have `database`, meaning it can be fully resolved withing the given database system.
 */
public class QueryResolver implements QueryVisitor {

    public static InstanceCategory run(QueryContext context, QueryNode rootNode) {
        return new QueryResolver(context, rootNode).run();
    }

    private final QueryContext context;
    private final QueryNode rootNode;

    private QueryResolver(QueryContext context, QueryNode rootNode) {
        this.context = context;
        this.rootNode = rootNode;
    }

    private InstanceCategory run() {
        rootNode.accept(this);

        // TODO
        return lastInstance;
    }

    private InstanceCategory lastInstance; // TODO - return ResultNode instead

    public void visit(DatabaseNode node) {
        final var translator = new QueryTranslator(context, node);
        final QueryStatement query = translator.run();

        final var databaseToInstance = new DatabaseToInstance();
        databaseToInstance.input(
            null, // TODO part.compiled.mapping,
            null,
            node.database.control.getPullWrapper(),
            PullQuery.fromString(query.stringContent())
        );
        final var instanceCategory = databaseToInstance.run();

        lastInstance = instanceCategory;
    }

    public void visit(PatternNode node) {
        throw QueryTreeException.unsupportedOutsideDatabase(node);
    }

    public void visit(FilterNode node) {
        if (node.filter instanceof ConditionFilter conditionFilter) {
            // TODO
        }
        else if (node.filter instanceof ValueFilter valueFilter) {
            // TODO
        }
    }

    public void visit(JoinNode node) {
        throw new UnsupportedOperationException();
    }

    public void visit(MinusNode node) {
        throw new UnsupportedOperationException();
    }

    public void visit(OptionalNode node) {
        throw new UnsupportedOperationException();
    }

    public void visit(UnionNode node) {
        throw new UnsupportedOperationException();
    }

}