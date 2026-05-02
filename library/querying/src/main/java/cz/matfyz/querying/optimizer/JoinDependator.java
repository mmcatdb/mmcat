package cz.matfyz.querying.optimizer;

import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.planner.QueryPlan;

public class JoinDependator {
    private static final double MAX_DEP_JOIN_ROWS = 3000;

    public static void run(QueryPlan queryPlan, CollectorCache cache) {
        GraphUtils.forEachDFS(queryPlan.root, node -> {
            if (!(node instanceof JoinNode joinNode)) return;

            QueryCostResolver.run(node, queryPlan.context, cache, false);

            final var fromCost = joinNode.fromChild().predictedCostData != null
                ? joinNode.fromChild().predictedCostData.rows() : null;
            final var toCost = joinNode.toChild().predictedCostData != null
                ? joinNode.toChild().predictedCostData.rows() : null;

            // TODO: dependent joins may be advantageous in other circumstances, like when filtering using an unindexed variable (so the added WHERE IN could filter through an indexed variable, which would then drastically reduce the need for other filtering), but that requires too much data unavailable here

            if (fromCost == null || toCost == null) {
                return;
            } else if (fromCost < toCost && fromCost <= MAX_DEP_JOIN_ROWS) {
                joinNode.forceDepJoinFromRef();
            } else if (fromCost > toCost && toCost <= MAX_DEP_JOIN_ROWS) {
                joinNode.forceDepJoinFromId();
            }
        });
    }
}
