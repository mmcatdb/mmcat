package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.abstractwrappers.utils.BaseQueryWrapper;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.StaticName;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Neo4jQueryWrapper extends BaseQueryWrapper implements AbstractQueryWrapper {

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
        // TODO fix
        return new TreeMap<>();
    }

    @Override public QueryStatement createDSLStatement() {
        StringBuilder sb = new StringBuilder();

        addMatches(sb);
        addWhere(sb);
        addWithReturn(sb);

        return new QueryStatement(new StringQuery(sb.toString()), context.rootStructure());
    }

    private void addMatches(StringBuilder sb) {
        if (joins.isEmpty()) {
            if (projections.isEmpty())
                throw QueryException.message("No tables are selected in MATCH clause.");

            final var onlyMapping = projections.getFirst().property().mapping;
            // TODO: how to figure out whether a kind is for a node or edge?
            sb.append("MATCH ").append(nodeName(onlyMapping)).append("\n");
            // sb.append("MATCH ").append(edgeName(onlyMapping)).append("\n");
            return;
        }

        throw new UnsupportedOperationException("Joins are not implemented for Neo4J yet.");
    }

    private void addWhere(StringBuilder sb) {
        if (filters.isEmpty())
            return;

        throw new UnsupportedOperationException("Filters are not implemented for Neo4J yet.");
    }

    private void addWithReturn(StringBuilder sb) {
        sb.append("WITH\n  ");

        sb.append(
            projections.stream()
            .map(p -> getProjectionSrc(p) + " AS " + getProjectionDst(p))
            .collect(Collectors.joining(",\n  "))
        );

        sb.append("\nRETURN\n  ");

        sb.append(
            projections.stream()
            .map(p -> getProjectionDst(p))
            .collect(Collectors.joining(",\n  "))
        );
    }

    private static String mappingVarName(Mapping mapping) {
        return "_v_" + mapping.kindName();
    }

    private String nodeName(Mapping mapping) {
        return "(" + mappingVarName(mapping) + ":" + mapping.kindName() + ")";
    }

    private String edgeName(Mapping mapping) {
        return "()-[" + mappingVarName(mapping) + ":" + mapping.kindName() + "]-()";
    }

    private String getProjectionSrc(Projection projection) {
        return getPropertyName(projection.property());
    }
    private String getProjectionDst(Projection projection) {
        return projection.structure().name;
    }

    private String getPropertyName(Property property) {
        if (property instanceof PropertyWithAggregation) {
            throw new UnsupportedOperationException("Aggregation was not implemented in Neo4J yet.");
        }
        return getPropertyNameWithoutAggregation(property);
    }

    private String getPropertyNameWithoutAggregation(Property property) {
        return mappingVarName(property.mapping) + "." + getRawAttributeName(property);
    }



    private String getRawAttributeName(Property property) {
        // Unlike fully relational DBs, Neo4j properties may also contain arrays of primitive types, but for now lets simplify
        // TODO: later solve this for arrays too (although that depends on how they can be queried)
        final var subpath = property.mapping.accessPath().getDirectSubpath(property.path);
        if (
            subpath == null ||
            !(subpath instanceof SimpleProperty simpleSubpath) ||
            !(simpleSubpath.name() instanceof StaticName staticName)
        )
            throw QueryException.propertyNotFoundInMapping(property);

        return staticName.getStringName();
    }

}
