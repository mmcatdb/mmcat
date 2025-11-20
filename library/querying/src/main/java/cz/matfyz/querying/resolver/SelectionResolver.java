package cz.matfyz.querying.resolver;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.core.querying.Computation;
import cz.matfyz.core.querying.Expression;
import cz.matfyz.core.querying.Expression.Constant;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.querying.core.JoinCandidate.JoinType;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.optimizer.CollectorCache;
import cz.matfyz.querying.planner.QueryPlan;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Maybe replace QueryResult for ListResult?
public class SelectionResolver implements QueryVisitor<QueryResult> {

    @SuppressWarnings({ "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectionResolver.class);

    public static QueryResult run(QueryPlan plan) {
        return new SelectionResolver(plan, null).run();
    }

    public static QueryResult run(QueryPlan plan, CollectorCache cache) {
        return new SelectionResolver(plan, cache).run();
    }

    private final QueryPlan plan;
    private final CollectorCache cache;
    private final ArrayList<Computation> selectionContext = new ArrayList<>(); // use as a "stack", or maybe in the future as Map<Variable, IdSet>

    private SelectionResolver(QueryPlan plan, CollectorCache cache) {
        this.plan = plan;
        this.cache = cache;
    }

    private QueryResult run() {
        return timedAccept(plan.root);
    }

    private QueryResult timedAccept(QueryNode node) {
        final long startNanos = System.nanoTime();
        final var result = node.accept(this);
        node.evaluationTimeInMs = (System.nanoTime() - startNanos) / 1_000_000.0;

        // in this case, Collector is not run and performance relies on node.evalutaionMillis
        // TODO: expand for other nodes
        // also TODO: add result size (even if approximate, like result.data.children().size() )
        if (cache != null && node instanceof DatasourceNode dsNode)
            cache.put(dsNode, null);

        return result;
    }

    public QueryResult visit(DatasourceNode node) {
        final var controlWrapper = plan.context.getProvider().getControlWrapper(node.datasource);

        if (selectionContext.size() > 0 &&
            controlWrapper.getQueryWrapper().isFilteringSupported()) {
            // TODO: also check if the selection variables are in the scope of this node (example:
            // A JOIN (B JOIN C)  with dependent join  A->B)
            // (see FilterDeepener.structureCoversVariables(), it is pretty much what we need)
            node.filters.addAll(selectionContext);
            selectionContext.removeIf(element -> true);
        }

        final QueryStatement query = DatasourceTranslator.run(plan.context, node);
        final var pullWrapper = controlWrapper.getPullWrapper();

        return pullWrapper.executeQuery(query);
    }

    public QueryResult visit(FilterNode node) {
        final var childResult = timedAccept(node.child());
        return node.tform.apply(childResult.data);
    }

    public QueryResult visit(JoinNode node) {
        if (node.candidate.type() == JoinType.Value)
            throw new UnsupportedOperationException("Joining by value is not implemented.");

        // TODO: Use collector cache to determine whether to use dependent join

        if (node.forceDepJoinFromRef) {
            final var refResult = timedAccept(node.fromChild());

            node.tform.applySource(refResult.data);
            final List<Expression> operands = new ArrayList<>();
            operands.add(node.candidate.variable());
            for (final var id : node.tform.getSourceIds()) {
                operands.add(new Constant(id));
            }

            selectionContext.add(plan.scope.computation.create(Computation.Operator.In, operands));

            final var idResult = timedAccept(node.toChild());
            return node.tform.applyTarget(idResult.data);

        } else if (node.forceDepJoinFromId) {
            final var idResult = timedAccept(node.toChild());

            final List<Expression> operands = new ArrayList<>();
            operands.add(node.candidate.variable());
            for (final var id : node.tform.getTargetIds(idResult.data)) {
                operands.add(new Constant(id));
            }

            selectionContext.add(plan.scope.computation.create(Computation.Operator.In, operands));

            final var refResult = timedAccept(node.fromChild());
            return node.tform.apply(refResult.data, idResult.data);

        } else {
            final var refResult = timedAccept(node.fromChild());
            final var idResult = timedAccept(node.toChild());

            return node.tform.apply(refResult.data, idResult.data);
        }
    }

    public QueryResult visit(MinusNode node) {
        throw new UnsupportedOperationException("SelectionResolver.visit(MinusNode) not implemented.");
    }

    public QueryResult visit(OptionalNode node) {
        throw new UnsupportedOperationException("SelectionResolver.visit(OptionalNode) not implemented.");
    }

    public QueryResult visit(UnionNode node) {
        throw new UnsupportedOperationException("SelectionResolver.visit(UnionNode) not implemented.");
    }

}
