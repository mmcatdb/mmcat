package cz.matfyz.querying.optimizer;

import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.planner.QueryPlan;
import cz.matfyz.querying.resolver.DatasourceTranslator;

public final class QueryCostResolver implements QueryVisitor<NodeCostData> {

    private QueryCostResolver(QueryContext context, CollectorCache cache, boolean overwrite) {
        // this.plan = plan;
        this.planContext = context;
        this.cache = cache;
        this.overwrite = overwrite;
    }

    public static NodeCostData run(QueryPlan plan, CollectorCache cache) {
        final var estimator = new QueryCostResolver(plan.context, cache, false);
        return estimator.acceptInto(plan.root);
    }

    public static NodeCostData run(QueryNode node, QueryContext context, CollectorCache cache, boolean overwrite) {
        final var estimator = new QueryCostResolver(context, cache, overwrite);
        return estimator.acceptInto(node);
    }

    // private final QueryPlan plan;
    private final QueryContext planContext;
    private final CollectorCache cache;
    private final boolean overwrite;

    private NodeCostData acceptInto(QueryNode child) {
        return !overwrite && child.predictedCostData != null
            ? child.predictedCostData
            : child.accept(this);
    }

    @Override public NodeCostData visit(DatasourceNode node) {
        // TODO: integrate into ControlWrapper
        // final var dsid = node.datasource.identifier;

        final var query = DatasourceTranslator.run(planContext, node);

        final var pullWrapper = planContext.getProvider().getControlWrapper(node.datasource).getPullWrapper();
        final var cachePredict = cache.predict(node);

        // try {
        //     final var collector = planContext.getProvider().getControlWrapper(node.datasource).getCollectorWrapper();
        //     final var dataModel = collector.executeQuery(query.content());

        //     cache.databaseData.put(dataModel.databaseID, dataModel.database);
        // }

        if (cachePredict != null) {
            node.predictedCostData = cachePredict;
        } else {
            final var explainResult = pullWrapper.explainQuery(query);

            node.predictedCostData = new NodeCostData(
                explainResult.rows(),
                explainResult.rows(),
                explainResult.timeMs()
            );
        }

        return node.predictedCostData;
    }

    @Override public NodeCostData visit(FilterNode node) {
        final var childPred = acceptInto(node.child());
        node.predictedCostData = new NodeCostData(
            childPred.network(),
            childPred.rows() != null ? childPred.rows() / 10 : null, // assumption of filter selectivity, no better mechanism for now
            childPred.timeMs()
        );
        return node.predictedCostData;
    }

    @Override public NodeCostData visit(JoinNode node) {
        final var from = acceptInto(node.fromChild());
        final var to = acceptInto(node.toChild());

        node.predictedCostData = new NodeCostData(
            from.network() != null && to.network() != null ? from.network() + to.network() : null,
            from.rows() != null && to.rows() != null ? Math.min(from.rows(), to.rows()) : null, // suboptimal, depends on cardinality between the two root objexes and various more complex factors; also does not work with a many-refs-per-record mongodb join (which is not supported yet)
            from.timeMs() != null && to.timeMs() != null ? from.timeMs() + to.timeMs() : null // also subopt.; add cost of operation
        );

        return node.predictedCostData;
    }



    @Override public NodeCostData visit(MinusNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override public NodeCostData visit(OptionalNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override public NodeCostData visit(UnionNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }
}
