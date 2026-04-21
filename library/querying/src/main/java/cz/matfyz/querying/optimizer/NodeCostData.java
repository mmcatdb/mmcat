package cz.matfyz.querying.optimizer;

/**
 * This class contains estimated cost data of a {@link cz.matfyz.querying.core.querytree.QueryNode}.
 * Null values mean "unknown".
 */
public record NodeCostData(
    /** Cost (size) of sending data over network */
    Double network,
    /** Number of records in the result */
    Double rows,
    // /** Size of each row */
    // Double rowSize,
    /** Query evaluation cost (filtering, joining, etc.) */
    Double timeMs
) {
    // Later on, this will probably contain more detailed data about the distribution of the query result so it can be used further.
    // Or just split into totalCost + additionalData

    public static NodeCostData unknown() {
        return new NodeCostData(null, null, null);
    }
}
