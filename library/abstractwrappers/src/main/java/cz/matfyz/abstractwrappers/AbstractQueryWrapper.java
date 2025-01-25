package cz.matfyz.abstractwrappers;

import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.QueryStructure;
import cz.matfyz.core.schema.SchemaObject;

import java.io.Serializable;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

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
    boolean isFilteringNotIndexedSupported();

    /**
     * Determines whether the aggregation functions are supported.
     */
    boolean isAggregationSupported();

    enum ComparisonOperator {
        Equal,
        NotEqual,
        Less,
        LessOrEqual,
        Greater,
        GreaterOrEqual,
    }

    enum AggregationOperator {
        Count,
        Sum,
        Min,
        Max,
        Average,
    }

    /**
     * A queryable property. It's defined by the mapping and a path from its root.
     *  - If the `parent` property is null, the path is relative to the root of the mapping.
     *  - Otherwise, the path is relative to the parent property.
     */
    class Property implements Comparable<Property>, Serializable {
        public final Mapping mapping;
        public final @Nullable Property parent;
        public final Signature path;
        public final SchemaObject schemaObject;

        public Property(Mapping mapping, Signature path, @Nullable Property parent) {
            this.mapping = mapping;
            this.path = path;
            this.parent = parent;
            this.schemaObject = findSchemaObject();
        }

        private SchemaObject findSchemaObject() {
            if (!path.isEmpty())
                return mapping.category().getEdge(path.getLast()).to();

            return parent == null
                ? mapping.rootObject()
                : parent.schemaObject;
        }

        @Override public int compareTo(Property other) {
            final int mappingComparison = mapping.compareTo(other.mapping);
            return mappingComparison != 0
                ? mappingComparison
                : schemaObject.compareTo(other.schemaObject);
        }

        @Override public boolean equals(Object other) {
            return (other instanceof Property otp) && compareTo(otp) == 0;
        }

        public Signature findFullPath() {
            if (parent == null)
                return path;

            return parent.findFullPath().concatenate(path);
        }

    }

    class PropertyWithAggregation extends Property {
        public final Signature aggregationRoot;
        public final AggregationOperator aggregationOperator;

        public PropertyWithAggregation(Mapping mapping, Signature path, @Nullable Property parent, Signature aggregationRoot, AggregationOperator aggregationOperator) {
            super(mapping, path, parent);
            this.aggregationRoot = aggregationRoot;
            this.aggregationOperator = aggregationOperator;
        }
    }

    record Constant(List<String> values) {}

    /**
     * Adds a projection to attribute which can eventually be optional (isOptional).
     */
    void addProjection(Property property, QueryStructure structure, boolean isOptional);

    public record JoinCondition(Signature from, Signature to) implements Serializable {}

    /**
     * Adds a join (or graph traversal).
     * @param from Mapping from which we are joining.
     * @param to Mapping to which we are joining.
     * @param conditions List of paths from both mappings to the joining properties.
     * @param repetition If not 1, the join will be recursive.
     * @param isOptional If true, the join will be optional.
     */
    void addJoin(Mapping from, Mapping to, List<JoinCondition> conditions, int repetition, boolean isOptional);

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

    public interface AbstractWrapperContext {

        QueryStructure rootStructure();

        /** Finds a property for given structure except for the root structure (which has no corresponding property). */
        Property getProperty(QueryStructure structure);

    }

    /**
     * Enables advanced mapping between the query structure and the access path.
     */
    void setContext(AbstractWrapperContext structure);

    record QueryStatement(QueryContent content, QueryStructure structure) {}

    /**
     * Builds a DSL statement based on the information obtained by calling the wrapper methods.
     */
    QueryStatement createDSLStatement();

}
