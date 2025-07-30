package cz.matfyz.querying.resolver;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.core.querying.Computation;
import cz.matfyz.core.querying.Expression;
import cz.matfyz.core.querying.Expression.Constant;
import cz.matfyz.core.querying.Expression.ExpressionScope;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.JoinCandidate.JoinType;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.planner.QueryPlan;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Maybe replace QueryResult for ListResult?
public class SelectionResolver implements QueryVisitor<QueryResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectionResolver.class);

    public static QueryResult run(QueryPlan plan) {
        return new SelectionResolver(plan.context, plan.root).run();
    }

    private final QueryContext context;
    private final QueryNode rootNode;
    private final ArrayList<Computation> selectionContext = new ArrayList<>(); // use as a "stack", or maybe in the future as Map<Variable, IdSet>

    private SelectionResolver(QueryContext context, QueryNode rootNode) {
        this.context = context;
        this.rootNode = rootNode;
    }

    private QueryResult run() {
        return timedAccept(rootNode);
    }

    private QueryResult timedAccept(QueryNode node) {
        final var startNanos = System.nanoTime();
        final var result = node.accept(this);
        final var nanos = (System.nanoTime() - startNanos) / 1_000_000;
        node.evaluationMillis = (int)(nanos);
        return result;
    }

    public QueryResult visit(DatasourceNode node) {
        final var controlWrapper = context.getProvider().getControlWrapper(node.datasource);

        if (selectionContext.size() > 0 &&
            controlWrapper.getQueryWrapper().isFilteringSupported()) {
            // TODO: also check if the selection variables are in the scope of this node (example:)
            // A JOIN (B JOIN C)  with dependent join  A->B
            node.filters.addAll(selectionContext);
            selectionContext.removeIf(element -> true);
        }

        final QueryStatement query = DatasourceTranslator.run(context, node);
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

        // TODO: Decide where to put the decision mechanism for whether dependent join is to be used (the condition for dependent join is: from result estimation is small and to res.est. is large)

        if (node.forceDepJoinFromId) {
            final var idResult = timedAccept(node.fromChild());

            node.tform.applySource(idResult.data);
            final List<Expression> operands = new ArrayList<>();
            operands.add(node.candidate.variable());
            for (final var id : node.tform.getSourceIds()) {
                operands.add(new Constant(id));
            }

            // TODO: either pass ExpressionScope from before or avoid it altogether
            selectionContext.add(new ExpressionScope().computation.create(Computation.Operator.In, operands));

            final var refResult = timedAccept(node.toChild());
            return node.tform.applyTarget(refResult.data);

        } else if (node.forceDepJoinFromRef) {
            final var refResult = timedAccept(node.toChild());

            final List<Expression> operands = new ArrayList<>();
            operands.add(node.candidate.variable());
            for (final var id : node.tform.getTargetIds(refResult.data)) {
                operands.add(new Constant(id));
            }

            // TODO: either pass ExpressionScope from before or avoid it altogether
            selectionContext.add(new ExpressionScope().computation.create(Computation.Operator.In, operands));

            final var idResult = timedAccept(node.fromChild());
            return node.tform.apply(idResult.data, refResult.data);

        } else {
            final var idResult = timedAccept(node.fromChild());
            final var refResult = timedAccept(node.toChild());

            return node.tform.apply(idResult.data, refResult.data);
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
