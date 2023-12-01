package cz.matfyz.abstractwrappers;

import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.utils.GraphUtils.Tree;
import cz.matfyz.core.utils.LineStringBuilder;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    boolean IsFilteringNotIndexedSupported();

    /**
     * Determines whether the aggregation functions are supported.
     */
    boolean isAggregationSupported();

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
    public static class Property implements Serializable {
        public final Kind kind;
        public final Signature path;

        public Property(Kind kind, Signature path) {
            this.kind = kind;
            this.path = path;
        }

        public SchemaObject findSchemaObject() {
            return path.isEmpty()
                ? kind.mapping.rootObject()
                : kind.mapping.category().getEdge(path.getLast()).to();
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
     * Defines the name of the root QueryStructure.
     */
    void defineRoot(String identifier);

    /**
     * Adds a projection to attribute hierarchicalPath which can eventually be optional (isOptional).
     */
    void addProjection(Property property, String identifier, boolean isOptional);

    public static record JoinCondition(Signature from, Signature to) {}

    /**
     * Adds a join (or graph traversal).
     * @param from Kind from which we are joining.
     * @param to Kind to which we are joining.
     * @param conditions List of paths from both kinds to the joining properties.
     * @param repetition If not 1, the join will be recursive.
     * @param isOptional If true, the join will be optional.
     */
    void addJoin(Kind from, Kind to, List<JoinCondition> conditions, int repetition, boolean isOptional);

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

    // TODO add to json conversion for FE. Also, probably move to a separate file.
    public static class QueryStructure implements Tree<QueryStructure> {
    
        public final String name;
        // TODO find out if the object is needed
        public final boolean isArray;
        public final Map<String, QueryStructure> children = new TreeMap<>();

        /** If null, this is the root of the tree. */
        @Nullable
        private QueryStructure parent;

        public QueryStructure(String name, boolean isArray) {
            this.name = name;
            this.isArray = isArray;
        }

        /**
         * Adds the child and returns it back.
         */
        public QueryStructure addChild(QueryStructure child) {
            this.children.put(child.name, child);
            child.parent = this;
            return child;
        }

        @Nullable
        public QueryStructure parent() {
            return parent;
        }

        @Override
        public Collection<QueryStructure> children() {
            return this.children.values();
        }

        private void print(LineStringBuilder builder) {
            builder.append(name);
            if (isArray)
                builder.append("[]");

            if (!children.isEmpty())
                builder.append(":");

            builder
                .down()
                .nextLine();

            children.values().forEach(child -> {
                child.print(builder);
                builder.nextLine();
            });
            builder.up();
        }

        @Override
        public String toString() {
            final var builder = new LineStringBuilder(0, "    ");
            print(builder);
            return builder.toString();
        }

        @Override
        public int compareTo(QueryStructure other) {
            return name.compareTo(other.name);
        }
    }

    public static record QueryStatement(String stringContent, QueryStructure structure) {}

    /**
     * Builds a DSL statement based on the information obtained by calling the wrapper methods.
     */
    QueryStatement createDSLStatement();

}
