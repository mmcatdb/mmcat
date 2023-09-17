package cz.matfyz.abstractwrappers;

import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.core.category.Signature;

import java.util.List;

public interface AbstractQueryWrapper {

    /**
     * Determines whether non-optional (inner) joins are supported.
     */
    boolean isJoinSupported();

    /**
     * Determines whether optional (left outer) joins are supported.
     */
    boolean isOptionalJoinSupported();

    /**
     * Determines whether recursive queries or their equivalents (e.g., recursive graph traversals) are supported.
     */
    boolean isRecursiveJoinSupported();

    /**
     * Determines whether filtering of values of a specific property is supported in general.
     */
    boolean isFilteringSupported();

    /**
     * Determines whether filtering of values of a specific property is supported if the property is not part of the identifier (key) of a specific kind.
     */
    boolean IsFilteringNotIndexedSupported();

    /**
     * Determines whether the aggregation functions are supported.
     */
    boolean isAggregationSupported();


    public static class VariableIdentifier implements Comparable<VariableIdentifier> {

        private String value;

        public VariableIdentifier(String value) {
            this.value = value;
        }

        @Override
        public int compareTo(VariableIdentifier other) {
            return value.compareTo(other.value);
        }

    }

    public enum ComparisonOperator {
        Equal,
        NotEqual,
        Less,
        LessOrEqual,
        Greater,
        GreaterOrEqual,
    }

    public enum AggregationOperator {
        Count,
        Sum,
        Min,
        Max,
        Average,
    }

    /**
     * This class represents a queryable property. It's defined by the kind and a path from its root.
     */
    public static class Property {
        public final Kind kind;
        public final Signature path;

        public Property(Kind kind, Signature path) {
            this.kind = kind;
            this.path = path;
        }
    }

    public static class PropertyWithAggregation extends Property {
        public final Signature aggregationRoot;
        public final AggregationOperator aggregationOperator;

        public PropertyWithAggregation(Kind kind, Signature path, Signature aggregationRoot, AggregationOperator aggregationOperator) {
            super(kind, path);
            this.aggregationRoot = aggregationRoot;
            this.aggregationOperator = aggregationOperator;
        }
    }

    public static record Constant(
        List<String> values
    ) {}

    /**
     * Adds a projection to attribute hierarchicalPath which can eventually be optional (isOptional).
     */
    void addProjection(Property property, boolean isOptional);

    /**
     * Adds a join (or graph traversal).
     * @param from Property from which we are joining.
     * @param to Property to which we are joining.
     * @param repetition If not 1, the join will be recursive.
     * @param isOptional If true, the join will be optional.
     */
    void addJoin(Property from, Property to, int repetition, boolean isOptional);

    /**
     * Adds a filtering between two variables or aggregations. E.g.:
     * FILTER(?price > ?minimalPrice),
     * FILTER(SUM(?price) > ?minimalTotal),
     * FILTER(?minimalTotal < SUM(?price)),
     * FILTER(SUM(?price1) < SUM(?price2)).
     */
    void addFilter(Property left, Property right, ComparisonOperator operator);

    /**
     * Adds a filtering between one variables or aggregation and one constant. E.g.:
     * FILTER(?price > 42),
     * FILTER(SUM(?price) > 69).
     */
    void addFilter(Property left, Constant right, ComparisonOperator operator);

    public static record QueryStatement(String stringContent) {}

    /**
     * Builds a DSL statement based on the information obtained by calling the wrapper methods.
     */
    QueryStatement createDSLStatement();
}
