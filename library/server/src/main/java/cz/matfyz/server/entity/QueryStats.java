package cz.matfyz.server.entity;

public record QueryStats(
    /** Total number of executions. */
    int executionCount,
    /** Total size of all results in bytes. */
    long resultSizeSumInBytes,
    /** Total planning time in milliseconds. Includes parsing, creating plans, and optimizations. */
    double planningTimeSumInMs,
    // FIXME We should probably split this to time in our system vs time in the underlying dbs.
    /** Total evaluation time in milliseconds. Includes selection and projection. */
    double evaluationTimeSumInMs
) {

    public static QueryStats empty() {
        return new QueryStats(0, 0L, 0, 0);
    }

    public QueryStats merge(QueryStats other) {
        return new QueryStats(
            this.executionCount + other.executionCount,
            this.resultSizeSumInBytes + other.resultSizeSumInBytes,
            this.planningTimeSumInMs + other.planningTimeSumInMs,
            this.evaluationTimeSumInMs + other.evaluationTimeSumInMs
        );
    }

}
