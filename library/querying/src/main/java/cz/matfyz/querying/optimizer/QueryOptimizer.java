package cz.matfyz.querying.optimizer;

import cz.matfyz.core.utils.Config;
import cz.matfyz.querying.planner.QueryPlan;

import org.checkerframework.checker.nullness.qual.Nullable;

public class QueryOptimizer {

    // Stuff in this class
    static boolean predicatePushdown = Config.GLOBAL.getBool("optimization.predicatePushdown");
    static boolean dependentJoins = Config.GLOBAL.getBool("optimization.dependentJoins");

    // Stuff in other classes (due to the query pipeline structure they cannot be put here)
    static boolean neo4jMatchConcatenation = Config.GLOBAL.getBool("optimization.neo4jMatchConcatenation");
    static boolean joinPlanSelection = Config.GLOBAL.getBool("optimization.joinPlanSelection");


    public static QueryPlan run(QueryPlan queryPlan, @Nullable CollectorCache cache) {

        if (predicatePushdown) {
            FilterDeepener.run(queryPlan);
        }

        if (dependentJoins && cache != null) {
            JoinDependator.run(queryPlan, cache);
        }

        // TODO Other optimization techniques:
        // - Split filters to further deepen them (WARNING: reference nodes and filtered nodes may change, some checks are required)
        // - Merge consecutive filters to reduce filter passes
        // - Possibly merge filters into joins directly below them? (requires a concept of theta-join)

        return queryPlan;
    }

}
