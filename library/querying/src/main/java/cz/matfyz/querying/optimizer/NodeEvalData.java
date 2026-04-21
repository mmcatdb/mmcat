package cz.matfyz.querying.optimizer;

/**
 * This class contains evaluation data of a {@link cz.matfyz.querying.core.querytree.QueryNode}.
 */
public record NodeEvalData(

    // /** Cost (size) of sending data over network */
    // double network,
    /** Number of records in the result */
    double rows,
    // /** Size of each row */
    // double rowSize,
    /** Query evaluation cost (filtering, joining, etc.) */
    double timeMs
) { }
