package cz.matfyz.abstractwrappers.utils;

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
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;
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

    // Root

    protected String rootIdentifier;

    public void defineRoot(String identifier) {
        this.rootIdentifier = identifier;
    }

    // Projections

    protected record Projection(Property property, String identifier, boolean isOptional) {}

    protected List<Projection> projections = new ArrayList<>();

    // TODO there should be some check if the projection isn't already defined. Probably by its variable? Or by the corresponding schema object?
    public void addProjection(Property property, String identifier, boolean isOptional) {
        projections.add(new Projection(property, identifier, isOptional));
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

    protected QueryStructure createStructure() {
        final var root = new QueryStructure(rootIdentifier, true);
        final Map<Property, QueryStructure> propertyToStructure = new TreeMap<>();

        for (final var projection : projections) {
            final var isArray = projection.property.path.hasDual();
            final var structure = new QueryStructure(projection.identifier, isArray);

            final var parent = findOrCreateStructureForInnerProperty(projection.property.parent, propertyToStructure, root);
            parent.addChild(structure);
        }

        return root;
    }

    private QueryStructure findOrCreateStructureForInnerProperty(
        @Nullable Property property,
        Map<Property, QueryStructure> propertyToStructure,
        QueryStructure root
    ) {
        if (property == null)
            return root;

        final var found = propertyToStructure.get(property);
        if (found != null)
            return found;

        final var isArray = property.path.hasDual();
        final var structure = new QueryStructure("TODO", isArray);
        propertyToStructure.put(property, structure);

        final var parent = findOrCreateStructureForInnerProperty(property.parent, propertyToStructure, root);
        parent.addChild(structure);

        return structure;
    }

}
