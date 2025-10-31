package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.abstractwrappers.querycontent.StringQuery;
import cz.matfyz.abstractwrappers.utils.BaseQueryWrapper;
import cz.matfyz.core.querying.Computation.Operator;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.Name.StringName;

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

    private static final Operators operators = new Operators();

    static {
        operators.define(Operator.Equal, "=");
        operators.define(Operator.NotEqual, "<>");
        operators.define(Operator.Less, "<");
        operators.define(Operator.LessOrEqual, "<=");
        operators.define(Operator.Greater, ">");
        operators.define(Operator.GreaterOrEqual, ">=");

        // TODO Aggregation operators.

        operators.define(Operator.In, "IN");
        operators.define(Operator.NotIn, "NOT IN");
    }

    @Override public QueryStatement createDSLStatement() {
        final var sb = new StringBuilder();

        addKindMatches(sb);
        addJoinMatches(sb);
        addWhere(sb);
        addWithReturn(sb);

        return new QueryStatement(new StringQuery(sb.toString()), context.rootStructure());
    }

    private void addKindMatches(StringBuilder sb) {
        for (final var projection : projections) {
            final var mapping = projection.property().mapping;

            String name = isRelationship(mapping) ? edgeName(mapping) : nodeName(mapping);
            sb.append("MATCH ").append(name).append("\n");
        }
    }

    /**
     * Adds matches specifying the way that kinds matched in {@link #addKindMatches(Stringbuilder)} should be joined.
     * For Neo4J, the from/to paths don't mostly matter (except for determining relationship direction using _from / _to) since only possible joins are node-relationship, which Neo4J handles independently of user-provided kind identifiers.
     */
    private void addJoinMatches(StringBuilder sb) {
        for (final var join : joins) {

            Mapping relationship;
            Mapping node;
            Signature relationshipPath;

            if (isRelationship(join.from()) && !isRelationship(join.to())) {
                relationship = join.from();
                node = join.to();
                relationshipPath = join.fromPath();
            }
            else if (!isRelationship(join.from()) && isRelationship(join.to())) {
                relationship = join.to();
                node = join.from();
                relationshipPath = join.toPath();
            }
            else {
                throw new UnsupportedOperationException("Graph join must be between node and edge.");
            }

            boolean directionIsTowardsNode = relationship.accessPath()
                .getDirectSubpath(relationshipPath.getFirst())
                .name().toString().startsWith(Neo4jControlWrapper.TO_NODE_PROPERTY_PREFIX);

            sb.append("MATCH (")
                .append(mappingVarName(node))
                .append(directionIsTowardsNode ? ")<-[" : ")-[")
                .append(mappingVarName(relationship))
                .append(directionIsTowardsNode ? "]-()" : "]->()")
                .append("\n");
        }
    }

    private void addWhere(StringBuilder sb) {
        if (filters.isEmpty())
            return;

        sb.append("WHERE\n  ");

        boolean first = true;
        for (final var f : filters) {
            if (first)
                first = false;
            else
                sb.append(" AND\n  ");

            if (f instanceof UnaryFilter uf)
                addFilter(uf, sb);
            else if (f instanceof BinaryFilter bf)
                addFilter(bf, sb);
            else if (f instanceof SetFilter sf)
                addFilter(sf, sb);
            else
                throw new UnsupportedOperationException("Unknown filter");
        }

        sb.append('\n');
    }

    private void addFilter(UnaryFilter filter, StringBuilder sb) {
        sb
            .append(getPropertyName(filter.property()))
            .append(" ")
            .append(operators.stringify(filter.operator()))
            // TODO Some sanitization should be done here.
            .append(" '")
            .append(filter.constant().value())
            .append("'");
    }

    private void addFilter(BinaryFilter filter, StringBuilder sb) {
        sb
            .append(getPropertyName(filter.property1()))
            .append(operators.stringify(filter.operator()))
            .append(getPropertyName(filter.property2()));
    }

    private void addFilter(SetFilter filter, StringBuilder sb) {
        sb.append(getPropertyName(filter.property()));

        final var values = filter.set();
        sb
            .append(" ")
            .append(operators.stringify(filter.operator()))
            .append(" [")
            .append(values.get(0));

        values.stream().skip(1).forEach(value -> sb.append(", ").append(value));

        sb.append("]");
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


    private static boolean isRelationship(Mapping mapping) {
        final boolean hasFrom = hasSubpathByPrefix(mapping.accessPath(), Neo4jControlWrapper.FROM_NODE_PROPERTY_PREFIX);
        final boolean hasTo = hasSubpathByPrefix(mapping.accessPath(), Neo4jControlWrapper.TO_NODE_PROPERTY_PREFIX);
        return hasFrom && hasTo;
    }

    private static boolean hasSubpathByPrefix(ComplexProperty path, String namePrefix) {
        for (final var subpath : path.subpaths()) {
            if ((subpath.name() instanceof final StringName stringName) && stringName.value.startsWith(namePrefix))
                return true;
        }
        return false;
    }

    private static String escapeName(String name) {
        return '`' + name + '`';
    }


    private static String mappingVarName(Mapping mapping) {
        return escapeName("VAR_" + mapping.kindName());
    }

    private String nodeName(Mapping mapping) {
        return "(" + mappingVarName(mapping) + ":" + escapeName(mapping.kindName()) + ")";
    }

    private String edgeName(Mapping mapping) {
        return "()-[" + mappingVarName(mapping) + ":" + escapeName(mapping.kindName()) + "]-()";
    }

    private String getProjectionSrc(Projection projection) {
        return getPropertyName(projection.property());
    }
    private String getProjectionDst(Projection projection) {
        return escapeName(projection.structure().name);
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
        // Neo4j properties may also contain arrays of primitive types
        // TODO: solve this for arrays too (although that depends on how they can be queried)
        final var subpath = property.mapping.accessPath().getDirectSubpath(property.path);
        if (
            subpath == null ||
            !(subpath instanceof SimpleProperty simpleSubpath) ||
            !(simpleSubpath.name() instanceof StringName stringName)
        )
            throw QueryException.propertyNotFoundInMapping(property);

        return escapeName(stringName.value);
    }

}
