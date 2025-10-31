package cz.matfyz.querying.optimizer;

import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperException;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.planner.QueryPlan;
import cz.matfyz.querying.resolver.DatasourceTranslator;

public final class QueryCostResolver implements QueryVisitor<NodeCostData> {

    private QueryCostResolver(QueryPlan plan, CollectorCache cache) {
        this.plan = plan;
        this.cache = cache;
    }

    public static NodeCostData run(QueryPlan plan, CollectorCache cache) {
        final var estimator = new QueryCostResolver(plan, cache);
        return plan.root.accept(estimator);
    }

    private final QueryPlan plan;
    private final CollectorCache cache;

    @Override
    public NodeCostData visit(DatasourceNode node) {
        try {
            // TODO: integrate into ControlWrapper
            // final var dsid = node.datasource.identifier;

            final var query = DatasourceTranslator.run(plan.context, node);

            final var collector = plan.context.getProvider().getControlWrapper(node.datasource).getCollectorWrapper();
            final var dataModel = collector.executeQuery(query.content());

            cache.databaseData.put(dataModel.databaseID, dataModel.database);

            node.predictedCostData = new NodeCostData(
                dataModel.result.resultTable.sizeInBytes,
                dataModel.result.resultTable.sizeInBytes, // parse time is estimated as O(data size)
                dataModel.result.executionTimeMillis
            );

            return node.predictedCostData;
        } catch (WrapperException e) {
            throw QueryException.message("Something went wrong: " + e.getMessage());
        }
    }

    @Override
    public NodeCostData visit(FilterNode node) {
        node.predictedCostData = node.child().accept(this);
        return node.predictedCostData;
    }

    @Override
    public NodeCostData visit(JoinNode node) {
        final var from = node.fromChild().accept(this);
        final var to = node.toChild().accept(this);

        node.predictedCostData = new NodeCostData(
            from.network() + to.network(),
            from.dataParse() + to.dataParse(),
            from.queryExecution() + to.queryExecution()
        );

        return node.predictedCostData;
    }



    @Override
    public NodeCostData visit(MinusNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public NodeCostData visit(OptionalNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public NodeCostData visit(UnionNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }
}
