package cz.matfyz.core.querying;

/**
 * This class represents the result of a selection part of a query. It contains
 * (a) data and (b) structure of the data.
 * Because we need to support all possible datasources, the data is represented
 * as a tree.
 */
public record ExplainResult(
    /** Null means unknown */
    Double rows,
    /** Null means unknown */
    Double rowSize,
    /** Null means unknown */
    Double timeMs
) {
    public static ExplainResult unknown() {
        return new ExplainResult(null, null, null);
    }
}
