package cz.matfyz.querying.optimizer;

import cz.matfyz.querying.planner.QueryPlan;

import org.checkerframework.checker.nullness.qual.Nullable;

public class QueryOptimizer {

    public static QueryPlan run(QueryPlan queryPlan, @Nullable CollectorCache cache) {

        FilterDeepener.run(queryPlan);

        if (cache != null) JoinDependator.run(queryPlan, cache);

        // TODO Other optimization techniques:
        // - Split filters to further deepen them (WARNING: reference nodes and filtered nodes may change, some checks are required)
        // - Merge consecutive filters to reduce filter passes
        // - Possibly merge filters into joins directly below them? (requires a concept of theta-join)

        return queryPlan;
    }

}
