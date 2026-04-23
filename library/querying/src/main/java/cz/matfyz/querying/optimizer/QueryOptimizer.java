package cz.matfyz.querying.optimizer;

import cz.matfyz.core.utils.Config;
import cz.matfyz.querying.planner.QueryPlan;

import org.checkerframework.checker.nullness.qual.Nullable;

public class QueryOptimizer {
    public static boolean predicatePushdown = Config.GLOBAL.getBool("optimization.predicatePushdown");
    public static boolean dependentJoins = Config.GLOBAL.getBool("optimization.dependentJoins");
    public static boolean fastPlanDrafting = Config.GLOBAL.getBool("optimization.fastPlanDrafting");


    public static QueryPlan run(QueryPlan queryPlan, @Nullable CollectorCache cache) {


        if (predicatePushdown) {
            FilterDeepener.run(queryPlan);
        }

        if (dependentJoins && cache != null) {
            JoinDependator.run(queryPlan, cache);
        }

        // TODO Other optimization techniques:
        // - Split filters to further deepen them (WARNING: reference nodes and filtered nodes may change, some checks are required)
        // - Merge consecutive filters to reduce filter passes (possibly even with non-filter nodes)
        // - Possibly merge filters into joins directly below them? (requires a concept of theta-join)

        return queryPlan;
    }

}
