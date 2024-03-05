package cz.matfyz.abstractwrappers.utils;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.AbstractWrapperContext;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.AggregationOperator;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Constant;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.JoinCondition;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Property;
import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.core.querying.QueryStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseQueryWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseQueryWrapper.class);

    protected abstract Map<ComparisonOperator, String> defineComparisonOperators();

    private final Map<ComparisonOperator, String> comparisonOperators = defineComparisonOperators();

    protected String getComparisonOperatorValue(ComparisonOperator operator) {
        var value = comparisonOperators.get(operator);
        if (value == null)
            throw QueryException.unsupportedOperator(operator);

        return value;
    }

    protected abstract Map<AggregationOperator, String> defineAggregationOperators();

    private final Map<AggregationOperator, String> aggregationOperators = defineAggregationOperators();

    protected String getAggregationOperatorValue(AggregationOperator operator) {
        var value = aggregationOperators.get(operator);
        if (value == null)
            throw QueryException.unsupportedOperator(operator);

        return value;
    }

    // Projections

    /**
     * @param structure A leaf in the query structure tree. Used to determine where to put the projection in the final result.
     */
    protected record Projection(Property property, QueryStructure structure, boolean isOptional) {}

    protected List<Projection> projections = new ArrayList<>();

    // TODO there should be some check if the projection isn't already defined. Probably by its variable? Or by the corresponding schema object?
    public void addProjection(Property property, QueryStructure structure, boolean isOptional) {
        projections.add(new Projection(property, structure, isOptional));
    }

    // Joins

    protected record Join(Kind from, Kind to, List<JoinCondition> conditions, int repetition, boolean isOptional) {}

    protected List<Join> joins = new ArrayList<>();

    public void addJoin(Kind from, Kind to, List<JoinCondition> conditions, int repetition, boolean isOptional) {
        joins.add(new Join(from, to, conditions, repetition, isOptional));
    }

    // Filters

    protected interface Filter {}

    protected record UnaryFilter(Property property, Constant constant, ComparisonOperator operator) implements Filter {}
    protected record BinaryFilter(Property property1, Property property2, ComparisonOperator operator) implements Filter {}

    protected List<Filter> filters = new ArrayList<>();

    public void addFilter(Property property1, Constant constant, ComparisonOperator operator) {
        filters.add(new UnaryFilter(property1, constant, operator));
    }

    public void addFilter(Property property1, Property property2, ComparisonOperator operator) {
        filters.add(new BinaryFilter(property1, property2, operator));
    }

    // Structure

    protected AbstractWrapperContext context;

    public void setContext(AbstractWrapperContext context) {
        this.context = context;
    }

}
