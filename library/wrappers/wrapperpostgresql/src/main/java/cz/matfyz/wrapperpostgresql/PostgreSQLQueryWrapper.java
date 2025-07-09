package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.abstractwrappers.utils.BaseQueryWrapper;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.querying.Computation.Operator;
import cz.matfyz.core.mapping.Name.StringName;

import java.util.List;
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

    private static final Operators operators = new Operators();

    static {

        operators.define(Operator.Equal, "=");
        operators.define(Operator.NotEqual, "<>");
        operators.define(Operator.Less, "<");
        operators.define(Operator.LessOrEqual, "<=");
        operators.define(Operator.Greater, ">");
        operators.define(Operator.GreaterOrEqual, ">=");

        operators.define(Operator.Count, "COUNT");
        operators.define(Operator.Sum, "SUM");
        operators.define(Operator.Min, "MIN");
        operators.define(Operator.Max, "MAX");
        operators.define(Operator.Average, "AVG");

        operators.define(Operator.In, "IN");
        operators.define(Operator.NotIn, "NOT IN");

    }

    private StringBuilder builder;
    private List<String> tableColumns;

    @Override public QueryStatement createDSLStatement() {
        builder = new StringBuilder();

        addSelect();
        addFrom();
        addWhere();

        return new QueryStatement(new PostgreSQLQuery(builder.toString(), tableColumns), context.rootStructure());
    }

    private void addSelect() {
        final String projectionsString = projections.stream()
            .map(projection -> "    " + getProjection(projection))
            .collect(Collectors.joining(",\n"));

        builder
            .append("SELECT\n")
            .append(projectionsString)
            .append("\n");

        tableColumns = projections.stream().map(projection -> getPropertyName(projection.property())).toList();
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

            // TODO there shouldn't be a null for the schema objex key ...
            final String fromProjection = getPropertyName(new Property(join.from(), join.fromPath(), null));
            final String toProjection = getPropertyName(new Property(join.to(), join.toPath(), null));
            final String condition = fromProjection + " = " + toProjection;

            builder
                .append(" JOIN ")
                .append(escapeName(newKind.kindName()))
                .append(" ON (")
                .append(condition)
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
        else if (filter instanceof SetFilter setFilter)
            addSetFilter(setFilter);

        builder.append("\n");
    }

    private void addUnaryFilter(UnaryFilter filter) {
        builder
            .append(getPropertyName(filter.property()))
            .append(" ")
            .append(operators.stringify(filter.operator()))
            // TODO Some sanitization should be done here.
            .append(" '")
            .append(filter.constant().value())
            .append("'");
    }

    private void addBinaryFilter(BinaryFilter filter) {
        builder
            .append(getPropertyName(filter.property1()))
            .append(operators.stringify(filter.operator()))
            .append(getPropertyName(filter.property2()));
    }

    private void addSetFilter(SetFilter filter) {
        builder.append(getPropertyName(filter.property()));

        final var values = filter.set();
        builder
            .append(" ")
            .append(operators.stringify(filter.operator()))
            .append(" (")
            .append(values.get(0));

        values.stream().skip(1).forEach(value -> builder.append(", ").append(value));

        builder.append(")");
    }

    private static String escapeName(String name) {
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
            !(simpleSubpath.name() instanceof StringName stringName)
        )
            throw QueryException.propertyNotFoundInMapping(property);

        return stringName.value;
    }

    private String getAggregationName(PropertyWithAggregation aggregation) {
        return operators.stringify(aggregation.operator) + "(" + getPropertyNameWithoutAggregation(aggregation) + ")";
    }

}
