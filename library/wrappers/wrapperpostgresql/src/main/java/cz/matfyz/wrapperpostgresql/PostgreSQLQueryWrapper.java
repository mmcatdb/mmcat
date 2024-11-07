package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.abstractwrappers.utils.BaseQueryWrapper;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class PostgreSQLQueryWrapper extends BaseQueryWrapper implements AbstractQueryWrapper {

    // CHECKSTYLE:OFF
    @Override public boolean isJoinSupported() { return true; }
    @Override public boolean isOptionalJoinSupported() { return true; }
    @Override public boolean isRecursiveJoinSupported() { return true; }
    @Override public boolean isFilteringSupported() { return true; }
    @Override public boolean isFilteringNotIndexedSupported() { return true; }
    @Override public boolean isAggregationSupported() { return true; }
    // CHECKSTYLE:ON

    @Override protected Map<ComparisonOperator, String> defineComparisonOperators() {
        final var output = new TreeMap<ComparisonOperator, String>();
        output.put(ComparisonOperator.Equal, "=");
        output.put(ComparisonOperator.NotEqual, "<>");
        output.put(ComparisonOperator.Less, "<");
        output.put(ComparisonOperator.LessOrEqual, "<=");
        output.put(ComparisonOperator.Greater, ">");
        output.put(ComparisonOperator.GreaterOrEqual, ">=");
        return output;
    }

    @Override protected Map<AggregationOperator, String> defineAggregationOperators() {
        final var output = new TreeMap<AggregationOperator, String>();
        output.put(AggregationOperator.Count, "COUNT");
        output.put(AggregationOperator.Sum, "SUM");
        output.put(AggregationOperator.Min, "MIN");
        output.put(AggregationOperator.Max, "MAX");
        output.put(AggregationOperator.Average, "AVG");
        return output;
    }

    private StringBuilder builder;

    @Override public QueryStatement createDSLStatement() {
        builder = new StringBuilder();

        addSelect();
        addFrom();
        addWhere();

        return new QueryStatement(new StringQuery(builder.toString()), context.rootStructure());
    }

    private void addSelect() {
        final String projectionsString = projections.stream()
            .map(projection -> "    " + getProjection(projection))
            .collect(Collectors.joining(",\n"));

        builder
            .append("SELECT\n")
            .append(projectionsString)
            .append("\n");
    }

    private void addFrom() {
        builder.append("FROM ");

        if (!joins.isEmpty()) {
            addJoins();
            return;
        }

        // final String firstKind = kinds.values().stream().findFirst().orElseThrow(() -> QueryException.message("No tables are selected in FROM clause."));
        if (projections.isEmpty())
            throw QueryException.message("No tables are selected in FROM clause.");

        final String kindName = projections.getFirst().property().mapping.kindName();
        builder
            .append(escapeName(kindName))
            .append("\n");
    }

    private void addJoins() {
        // TODO add support for optional joins

        final var joinedKinds = new TreeSet<Mapping>();

        final Mapping fromKind = joins.get(0).from();
        joinedKinds.add(fromKind);

        builder
            .append(escapeName(fromKind.kindName()))
            .append("\n");

        for (final var join : joins) {
            Mapping newKind;
            if (!joinedKinds.contains(join.to()))
                newKind = join.to();
            else if (!joinedKinds.contains(join.from()))
                newKind = join.from();
            else
                continue;

            joinedKinds.add(newKind);

            final String conditions = join.conditions().stream().map(condition -> {
                // TODO there shouldn't be a null for the schema object key ...
                final String fromProjection = getPropertyName(new Property(join.from(), condition.from(), null));
                final String toProjection = getPropertyName(new Property(join.to(), condition.to(), null));
                return fromProjection + " = " + toProjection;
            })
                .collect(Collectors.joining(" AND "));

            builder
                .append(" JOIN ")
                .append(escapeName(newKind.kindName()))
                .append(" ON (")
                .append(conditions)
                .append(")\n");
        }
    }

    private void addWhere() {
        if (filters.isEmpty())
            return;

        builder.append("WHERE ");
        addFilter(filters.get(0));

        filters.stream().skip(1).forEach(filter -> {
            builder.append("\nAND ");
            addFilter(filter);
        });
    }

    private void addFilter(Filter filter) {
        if (filter instanceof UnaryFilter unaryFilter)
            addUnaryFilter(unaryFilter);
        else if (filter instanceof BinaryFilter binaryFilter)
            addBinaryFilter(binaryFilter);

        builder.append("\n");
    }

    private void addUnaryFilter(UnaryFilter filter) {
        builder.append(getPropertyName(filter.property()));

        final var values = filter.constant().values();
        if (values.size() == 1) {
            builder
                .append(" ")
                .append(getComparisonOperatorValue(filter.operator()))
                .append(" '")
                .append(values.get(0))
                .append("'");

            return;
        }

        if (filter.operator() != ComparisonOperator.Equal)
            throw QueryException.message("Operator " + filter.operator() + " can't be used for multiple values.");

        builder
            .append(" IN (")
            .append(values.get(0));

        values.stream().skip(1).forEach(value -> builder.append(", ").append(value));

        builder.append(")");
    }

    private void addBinaryFilter(BinaryFilter filter) {
        builder
            .append(getPropertyName(filter.property1()))
            .append(getComparisonOperatorValue(filter.operator()))
            .append(getPropertyName(filter.property2()));
    }

    private String escapeName(String name) {
        return "\"" + name + "\"";
    }


    private String getProjection(Projection projection) {
        return getPropertyName(projection.property()) + " AS " + escapeName(projection.structure().name);
    }

    private String getPropertyName(Property property) {
        return property instanceof PropertyWithAggregation aggregation
            ? getAggregationName(aggregation)
            : getPropertyNameWithoutAggregation(property);
    }

    private String getPropertyNameWithoutAggregation(Property property) {
        return escapeName(property.mapping.kindName()) + "." + escapeName(getRawAttributeName(property));
    }

    private String getRawAttributeName(Property property) {
        // Direct subpath is ok since the postgresql mapping must be flat.
        final var subpath = property.mapping.accessPath().getDirectSubpath(property.path);
        if (
            subpath == null ||
            !(subpath instanceof SimpleProperty simpleSubpath) ||
            !(simpleSubpath.name() instanceof StaticName staticName)
        )
            throw QueryException.propertyNotFoundInMapping(property);

        return staticName.getStringName();
    }

    private String getAggregationName(PropertyWithAggregation aggregation) {
        return getAggregationOperatorValue(aggregation.aggregationOperator) + "(" + getPropertyNameWithoutAggregation(aggregation) + ")";
    }

}
