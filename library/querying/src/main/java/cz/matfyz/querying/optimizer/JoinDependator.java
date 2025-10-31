package cz.matfyz.querying.optimizer;

import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.planner.QueryPlan;

public class JoinDependator implements QueryVisitor<Integer> {

    private final QueryPlan plan;
    private final CollectorCache cache;

    private JoinDependator(QueryPlan plan, CollectorCache cache) {
        this.plan = plan;
        this.cache = cache;
    }

    public static void run(QueryPlan queryPlan, CollectorCache cache) {
        queryPlan.root.accept(new JoinDependator(queryPlan, cache));
    }

    @Override
    public Integer visit(DatasourceNode node) {
        return cache.predict(node);
    }

    @Override
    public Integer visit(FilterNode node) {
        // TODO: estimate filter selectivity
        return node.child().accept(this);
    }

    @Override
    public Integer visit(JoinNode node) {
        final var fromCost = node.fromChild().accept(this);
        final var toCost = node.toChild().accept(this);

        // TODO: dependent joins may be advantageous in other circumstances, like when filtering using an unindexed variable (so the added WHERE IN could filter through an indexed variable, which would then drastically reduce the need for other filtering), but that requires too much data unavailable here

        if (fromCost == null || toCost == null) {
            return null;
        } else if (fromCost < toCost) {
            node.forceDepJoinFromRef();
        } else if (fromCost > toCost) {
            node.forceDepJoinFromId();
        }

        // TODO: this estimation is extremely rough; maybe improve
        return fromCost * 2;
    }

    @Override
    public Integer visit(MinusNode node) {
        return null;
    }

    @Override
    public Integer visit(OptionalNode node) {
        return null;
    }

    @Override
    public Integer visit(UnionNode node) {
        return null;
    }

}
