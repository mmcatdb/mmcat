package cz.matfyz.querying.optimizer;

import cz.matfyz.querying.planner.QueryPlan;

public class QueryOptimizer {

    public static QueryPlan run(QueryPlan plan) {
        return new QueryOptimizer(plan).run();
    }

    private final QueryPlan original;

    private QueryOptimizer(QueryPlan original) {
        this.original = original;
    }

    private QueryPlan run() {

        // TODO

        return original;
    }

}
