package cz.matfyz.abstractwrappers;

import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.Computation.Operator;
import cz.matfyz.core.querying.Expression.Constant;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.core.schema.SchemaObjex;

import java.io.Serializable;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This wrapper reads ('pulls') data from a datasource.
 * Processing of the data is then passed to other wrappers.
 */
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

    /**
     * A queryable property. It's defined by the mapping and a path from its root.
     *  - If the `parent` property is null, the path is relative to the root of the mapping.
     *  - Otherwise, the path is relative to the parent property.
     */
    class Property implements Comparable<Property>, Serializable {

        public final Mapping mapping;
        public final @Nullable Property parent;
        public final Signature path;
        public final SchemaObjex schemaObjex;

        public Property(Mapping mapping, Signature path, @Nullable Property parent) {
            this.mapping = mapping;
            this.path = path;
            this.parent = parent;
            this.schemaObjex = findSchemaObjex();
        }

        private SchemaObjex findSchemaObjex() {
            if (!path.isEmpty())
                return mapping.category().getEdge(path.getLast()).to();

            return parent == null
                ? mapping.rootObjex()
                : parent.schemaObjex;
        }

        @Override public int compareTo(Property other) {
            final int mappingComparison = mapping.compareTo(other.mapping);
            return mappingComparison != 0
                ? mappingComparison
                : schemaObjex.compareTo(other.schemaObjex);
        }

        @Override public boolean equals(Object other) {
            return (other instanceof Property otp) && compareTo(otp) == 0;
        }

        public Signature findFullPath() {
            if (parent == null)
                return path;

            return parent.findFullPath().concatenate(path);
        }

        @Override public String toString() {
            return "{ " + mapping + ": " + path + " (" + findFullPath() + ") }";
        }

    }

    class PropertyWithAggregation extends Property {
        public final Signature aggregationRoot;
        public final Operator operator;

        public PropertyWithAggregation(Mapping mapping, Signature path, @Nullable Property parent, Signature aggregationRoot, Operator operator) {
            super(mapping, path, parent);
            this.aggregationRoot = aggregationRoot;
            this.operator = operator;
        }
    }

    /**
     * Adds a projection to attribute which can eventually be optional (isOptional).
     */
    void addProjection(Property property, ResultStructure structure, boolean isOptional);

    /**
     * Adds a join (or graph traversal).
     * @param from Mapping from which we are joining.
     * @param to Mapping to which we are joining.
     * @param condition Contains paths from both mappings to the joining properties.
     * @param repetition If not 1, the join will be recursive.
     * @param isOptional If true, the join will be optional.
     */
    void addJoin(Mapping from, Mapping to, Signature fromPath, Signature toPath, int repetition, boolean isOptional);

    /**
     * Adds a filtering between one variables or aggregation and one constant. E.g.:
     * FILTER(?price > 42),
     * FILTER(SUM(?price) > 69).
     */
    void addFilter(Property property, Constant constant, Operator operator);

    /**
     * Adds a filtering between two variables or aggregations. E.g.:
     * FILTER(?price > ?minimalPrice),
     * FILTER(SUM(?price) > ?minimalTotal),
     * FILTER(?minimalTotal < SUM(?price)),
     * FILTER(SUM(?price1) < SUM(?price2)).
     */
    void addFilter(Property property1, Property property2, Operator operator);

    /**
     * Adds a filtering between one variable or aggregation and a set of constants. E.g.:
     * FILTER(?price IN (42, 69)),
     * FILTER(SUM(?price) NOT IN (69, 42)).
     */
    void addFilter(Property property, List<Constant> set, Operator operator);

    public interface AbstractWrapperContext {

        ResultStructure rootStructure();

        /** Finds a property for given structure except for the root structure (which has no corresponding property). */
        Property getProperty(ResultStructure structure);

    }

    /**
     * Enables advanced mapping between the result structure and the access path.
     */
    void setContext(AbstractWrapperContext structure);

    record QueryStatement(QueryContent content, ResultStructure structure) {}

    /**
     * Builds a DSL statement based on the information obtained by calling the wrapper methods.
     */
    QueryStatement createDSLStatement();

}
