package cz.matfyz.server.querying;

public record QueryStats(
    /** Total number of executions. */
    int executionCount,
    /** Total size of all results in bytes. */
    AggregatedLong resultSizeInBytes,
    /** Total planning time in milliseconds. Includes parsing, creating plans, and optimizations. */
    AggregatedDouble planningTimeInMs,
    // FIXME We should probably split this to time in our system vs time in the underlying dbs.
    /** Total evaluation time in milliseconds. Includes selection and projection. */
    AggregatedDouble evaluationTimeInMs
) {

    public static QueryStats scalar(
        long resultSizeInBytes,
        double planningTimeInMs,
        double evaluationTimeInMs
    ) {
        return new QueryStats(
            1,
            AggregatedLong.scalar(resultSizeInBytes),
            AggregatedDouble.scalar(planningTimeInMs),
            AggregatedDouble.scalar(evaluationTimeInMs)
        );
    }

    public QueryStats merge(QueryStats other) {
        return new QueryStats(
            this.executionCount + other.executionCount,
            this.resultSizeInBytes.merge(other.resultSizeInBytes),
            this.planningTimeInMs.merge(other.planningTimeInMs),
            this.evaluationTimeInMs.merge(other.evaluationTimeInMs)
        );
    }

    public record AggregatedLong(
        long min,
        long max,
        long sum
    ) {

        public double average(int count) {
            // The explicit cast is needed to prevent integer division (it also causes NaN when count is zero).
            return (double) sum / count;
        }

        public static AggregatedLong scalar(long value) {
            return new AggregatedLong(value, value, value);
        }

        public AggregatedLong merge(AggregatedLong other) {
            return new AggregatedLong(
                Math.min(this.min, other.min),
                Math.max(this.max, other.max),
                this.sum + other.sum
            );
        }

    }

    public record AggregatedDouble(
        double min,
        double max,
        double sum
    ) {

        public double average(int count) {
            return sum / count;
        }

        public static AggregatedDouble scalar(double value) {
            return new AggregatedDouble(value, value, value);
        }

        public AggregatedDouble merge(AggregatedDouble other) {
            return new AggregatedDouble(
                Math.min(this.min, other.min),
                Math.max(this.max, other.max),
                this.sum + other.sum
            );
        }

    }

}
