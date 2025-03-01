package cz.matfyz.querying.optimizer;

import cz.matfyz.querying.planner.QueryPlan;

public class QueryOptimizer {

    public static QueryPlan run(QueryPlan plan) {
        return new QueryOptimizer(plan).run();
    }

    private final QueryPlan queryPlan;

    private QueryOptimizer(QueryPlan original) {
        this.queryPlan = original;
    }

    private QueryPlan run() {

        FilterDeepener.run(queryPlan);

        // TODO Other optimization techniques:
        // - Split filters to further deepen them
        // - Merge consecutive filters to reduce filter passes
        // - Possibly merge filters into joins directly below them? (requires a concept of theta-join)

        return queryPlan;
    }

}
