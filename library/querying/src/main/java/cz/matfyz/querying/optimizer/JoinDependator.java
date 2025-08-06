package cz.matfyz.querying.optimizer;

import cz.matfyz.core.collector.CollectorCache;
import cz.matfyz.core.collector.DataModel.TableData;
import cz.matfyz.core.querying.Computation;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.querying.Computation.Operator;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.planner.QueryPlan;

public class JoinDependator implements QueryVisitor<TableData> {

    private final QueryPlan plan;
    private final CollectorCache cache;

    private JoinDependator(QueryPlan plan, CollectorCache cache) {
        this.plan = plan;
        this.cache = cache;
    }

    public static void run(QueryPlan queryPlan, CollectorCache cache) {
        queryPlan.root.accept(new JoinDependator(queryPlan, cache));
    }

    private TableData filter(TableData resultData, Computation filter) {
        if (filter.operator == Operator.In || filter.operator == Operator.Equal) {
            final var filterVariable = (Variable)(filter.arguments.get(0));

            // TODO: change filterVariable to resultStructure child
            Double ratio = resultData.getColumn(filterVariable.name(), false).distinctRatio;
            if (ratio == null) ratio = -1.0;

            double selectivity = 1.0 / 10_000;
            if (ratio >= 0) selectivity = resultData.rowCount / ratio;
            else if (ratio > -1) selectivity = -ratio;
            selectivity *= filter.arguments.size() - 1; // for set operator

            resultData.rowCount = (long)Math.ceil(resultData.rowCount * selectivity);
            resultData.sizeInBytes = (long)Math.ceil(resultData.sizeInBytes * selectivity);
            resultData.sizeInPages = (long)Math.ceil(resultData.sizeInPages * selectivity);
            return resultData;
        } else return resultData; // in the worst case, it is as big as the original
        // TODO: estimate filter selectivity in better ways
    }

    @Override
    public TableData visit(DatasourceNode node) {
        if (node.kinds.size() == 1) {
            final var kind = node.kinds.iterator().next().kind;

            final var cachedDB = cache.databaseData.get(node.datasource.identifier);
            if (cachedDB == null) return null;
            var tableData = cachedDB.getTable(kind.kindName(), false);

            if (node.filters.size() > 0) {
                tableData = tableData.clone();
                for (final var filter : node.filters) filter(tableData, filter);
            }

            return tableData;
        }

        return null; // TODO: either estimate join directly or through cached queries in CollectorCache
    }

    @Override
    public TableData visit(FilterNode node) {
        final var resultData = node.child().accept(this);
        if (resultData == null) return null;

        final var newResult = resultData.clone();
        return filter(newResult, node.filter);
    }

    @Override
    public TableData visit(JoinNode node) {
        final Long FEW_BYTES = 1_000_000L; // around a MB could be OK

        final var fromData = node.fromChild().accept(this);
        final var toData = node.toChild().accept(this);

        // TODO: dependent joins may be advantageous in other circumstances, like when filtering using an unindexed variable (so the added WHERE IN could filter through an indexed variable, which would then drastically reduce the need for other filtering)
        if (fromData != null && fromData.sizeInBytes <= FEW_BYTES && (toData == null || toData.sizeInBytes > FEW_BYTES)) {
            node.forceDepJoinFromId();
        }
        else if (toData != null && toData.sizeInBytes <= FEW_BYTES && (fromData == null || fromData.sizeInBytes > FEW_BYTES)) {
            node.forceDepJoinFromRef();
        }

        // TODO: merge with columns of toData, update sizeInBytes, etc.
        return fromData;
    }

    @Override
    public TableData visit(MinusNode node) {
        return null;
    }

    @Override
    public TableData visit(OptionalNode node) {
        return null;
    }

    @Override
    public TableData visit(UnionNode node) {
        return null;
    }

}
