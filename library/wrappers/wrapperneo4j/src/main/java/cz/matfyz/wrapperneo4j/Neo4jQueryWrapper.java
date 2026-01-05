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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;

public class Neo4jQueryWrapper extends BaseQueryWrapper implements AbstractQueryWrapper {

    // CHECKSTYLE:OFF
    @Override public boolean isJoinSupported() { return true; }
    @Override public boolean isOptionalJoinSupported() { return true; }
    @Override public boolean isRecursiveJoinSupported() { return true; }
    @Override public boolean isFilterSupported(Operator operator) { return operators.isSupported(operator); }
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

        // TODO Logical operators.
        // TODO Aggregation operators.

        operators.define(Operator.In, "IN");
        operators.define(Operator.NotIn, "NOT IN");
    }

    final StringBuilder sb = new StringBuilder();

    @Override public QueryStatement createDSLStatement() {
        // addKindMatches();
        // addJoinMatches();
        addMatches();
        addWhere();
        addWith();
        addReturn();

        return new QueryStatement(new StringQuery(sb.toString()), context.rootStructure());
    }

    private static class MatchClauseChain {
        public final ArrayDeque<Mapping> chain = new ArrayDeque<>();
        public final ArrayDeque<Boolean> chainElementRelationshipIsToRight = new ArrayDeque<>();

        public MatchClauseChain(Mapping mapping1, Mapping mapping2, Signature path1, Signature path2) {
            chain.addLast(mapping1);
            chain.addLast(mapping2);
            chainElementRelationshipIsToRight.addLast(
                isRelationship(mapping1) && isToDirection(mapping1, path1)
            );
            chainElementRelationshipIsToRight.addLast(
                isRelationship(mapping2) && !isToDirection(mapping2, path2)
            );
        }

        public boolean tryJoin(Mapping newMapping, Mapping existingMapping, Signature newMappingPath) {
            if (chain.getLast() == existingMapping) {
                chain.addLast(newMapping);
                chainElementRelationshipIsToRight.addLast(
                    isRelationship(newMapping) && !isToDirection(newMapping, newMappingPath)
                );

                return true;
            } else if (chain.getFirst() == existingMapping) {
                chain.addFirst(newMapping);
                chainElementRelationshipIsToRight.addFirst(
                    isRelationship(newMapping) && isToDirection(newMapping, newMappingPath)
                );

                return true;
            }
            return false;
        }
    }

    private void addMatches() {
        if (joins.isEmpty()) {
            final var mapping = projections.get(0).property().mapping;
            sb.append("MATCH ");
            if (isRelationship(mapping)) {
               addRelationshipName(mapping);
            } else {
                addNodeName(mapping);
            }
            sb.append("\n");
            return;
        }

        // Check if the join is supported (so far multiple of the same kind do not produce results)
        // WARNING: this might happen in other scenarios too, but checking it would be too difficult anyway...
        for (final var join : joins) {
            if (
                (isRelationship(join.from()) && findFromName(join.from()).equals(findToName(join.from()))) ||
                (isRelationship(join.to()) && findFromName(join.to()).equals(findToName(join.to())))
            ) {
                throw new RuntimeException("Neo4jQueryWrapper: Multiple references to same kind are not supported.");
            }
        }

        // Each MATCH clause is a "chain" of joined kinds. For more complex structures than just graph paths, we need multiple chains.
        final var chains = new ArrayList<MatchClauseChain>();
        final var firstJoin = joins.get(0);
        chains.add(new MatchClauseChain(firstJoin.from(), firstJoin.to(), firstJoin.fromPath(), firstJoin.toPath()));

        final var joinedKinds = new TreeSet<Mapping>();
        joinedKinds.add(firstJoin.from());
        joinedKinds.add(firstJoin.to());

        boolean newJoinsAdded = true;
        while (newJoinsAdded) {
            newJoinsAdded = false;

            for (final var join : joins) {
                Mapping newKind, existingKind;
                Signature newKindPath;
                if (joinedKinds.contains(join.from()) && !joinedKinds.contains(join.to())) {
                    newKind = join.to();
                    existingKind = join.from();
                    newKindPath = join.toPath();
                } else if (joinedKinds.contains(join.to()) && !joinedKinds.contains(join.from())) {
                    newKind = join.from();
                    existingKind = join.to();
                    newKindPath = join.fromPath();
                } else {
                    continue;
                }

                newJoinsAdded = true;
                joinedKinds.add(newKind);

                boolean appendedToChain = false;
                for (final var chain : chains) {
                    if (chain.tryJoin(newKind, existingKind, newKindPath)) {
                        appendedToChain = true;
                        break;
                    }
                }

                if (!appendedToChain) { // Can't append? Create a new chain
                    chains.add(new MatchClauseChain(join.from(), join.to(), join.fromPath(), join.toPath()));
                }
            }
        }

        final var declaredKinds = new TreeSet<Mapping>();

        final Consumer<Mapping> addKindId = (Mapping mapping) -> {
            sb.append(mappingVarName(mapping));
            if (declaredKinds.add(mapping)) {
                sb.append(':').append(escapeName(mapping.kindName()));
            }
        };

        for (final var chain : chains) {
            sb.append("MATCH ");
            Mapping previous = null;
            final var kindRightIter = chain.chainElementRelationshipIsToRight.iterator();
            for (final var kind : chain.chain) {
                final var kindRight = kindRightIter.next();
                if (previous == null) {
                    if (!isRelationship(kind)) {
                        sb.append('(');
                        addKindId.accept(kind);
                        sb.append(')');
                    } else if (kindRight) {
                        sb.append('(');
                        sb.append(mappingVarNameFrom(kind));
                        sb.append(")-[");
                        addKindId.accept(kind);
                        sb.append("]->");
                    } else {
                        sb.append('(');
                        sb.append(mappingVarNameTo(kind));
                        sb.append(")<-[");
                        addKindId.accept(kind);
                        sb.append("]-");
                    }

                    previous = kind;
                    continue;
                }

                if (!isRelationship(kind)) {
                    sb.append('(');
                    addKindId.accept(kind);
                    sb.append(')');
                    previous = kind;
                    continue;
                }

                if (isRelationship(previous)) {
                    sb.append('(');
                    sb.append(kindRight ? mappingVarNameFrom(kind) : mappingVarNameTo(kind)); // TODO: change mappingVarNameFrom to something more universal (ideally based on the actual kind); maybe actually refactor it inside the method
                    sb.append(')');
                }

                if (kindRight) {
                    sb.append("-[");
                    addKindId.accept(kind);
                    sb.append("]->");
                } else {
                    sb.append("<-[");
                    addKindId.accept(kind);
                    sb.append("]-");
                }

                previous = kind;
                // Holy fuck this is bullshit...
            }

            if (isRelationship(previous)) {
                sb.append('(');
                sb.append(chain.chainElementRelationshipIsToRight.getLast() ? mappingVarNameTo(previous) : mappingVarNameFrom(previous));
                sb.append(')');
            }

            sb.append('\n');
        }
    }

    private void addKindMatches() {
        if (joins.isEmpty()) {
            final var mapping = projections.get(0).property().mapping;
            sb.append("MATCH ");
            if (isRelationship(mapping)) {
               addRelationshipName(mapping);
            } else {
                addNodeName(mapping);
            }
            sb.append("\n");
            return;
        }

        final var joinedKinds = new HashSet<Mapping>();
        for (final var projection : projections) {
            joinedKinds.add(projection.property().mapping);
        }
        for (final var mapping : joinedKinds) {
            sb.append("MATCH ");
            if (isRelationship(mapping)) {
               addRelationshipName(mapping);
            } else {
                addNodeName(mapping);
            }
            sb.append("\n");
        }
    }

    /**
     * Adds matches specifying the way that kinds matched in {@link #addKindMatches(Stringbuilder)} should be joined.
     * For Neo4J, the from/to paths don't mostly matter (except for determining relationship direction using _from / _to) since only possible joins are node-relationship, which Neo4J handles independently of user-provided kind identifiers.
     */
    private void addJoinMatches() {
        for (final var join : joins) {

            Mapping relationship;
            Mapping node;
            Signature relationshipPath;

            if (isRelationship(join.from()) && isRelationship(join.to())) {
                // Special case
                sb.append("MATCH ()-[")
                    .append(mappingVarName(join.from()))
                    .append("]-()-[")
                    .append(mappingVarName(join.to()))
                    .append("]-()\n");
                continue;

            } else if (isRelationship(join.from()) && !isRelationship(join.to())) {
                relationship = join.from();
                node = join.to();
                relationshipPath = join.fromPath();
            } else if (!isRelationship(join.from()) && isRelationship(join.to())) {
                relationship = join.to();
                node = join.from();
                relationshipPath = join.toPath();
            } else {
                throw new UnsupportedOperationException("Graph cannot be between 2 nodes.");
            }

            boolean directionIsTowardsNode = false;
            if (!relationshipPath.isEmpty()) {
                directionIsTowardsNode = relationship.accessPath()
                    .getPropertyPath(relationshipPath).get(0)
                    .name().toString().startsWith(Neo4jControlWrapper.TO_NODE_PROPERTY_PREFIX);
            } else {
                for (final var aPath : relationship.accessPath().subpaths()) {
                    if (aPath.signature().isEmpty() && aPath.name().toString().startsWith(Neo4jControlWrapper.TO_NODE_PROPERTY_PREFIX)) {
                        directionIsTowardsNode = true;
                        break;
                    }
                }
            }

            sb.append("MATCH (")
                .append(mappingVarName(node))
                .append(directionIsTowardsNode ? ")<-[" : ")-[")
                .append(mappingVarName(relationship))
                .append(directionIsTowardsNode ? "]-()" : "]->()")
                .append("\n");
        }
    }

    /** Returns whether a path from a relationship is in the relationship's "_to" property or not. */
    private static boolean isToDirection(Mapping relationshipKind, Signature path) {
        if (!path.isEmpty()) {
            return relationshipKind.accessPath()
                .getPropertyPath(path).get(0)
                .name().toString().startsWith(Neo4jControlWrapper.TO_NODE_PROPERTY_PREFIX);
        }

        for (final var aPath : relationshipKind.accessPath().subpaths()) {
            if (aPath.signature().isEmpty() && aPath.name().toString().startsWith(Neo4jControlWrapper.TO_NODE_PROPERTY_PREFIX)) {
                return true;
            }
        }
        return false;
    }

    private void addWhere() {
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
                addFilter(uf);
            else if (f instanceof BinaryFilter bf)
                addFilter(bf);
            else if (f instanceof SetFilter sf)
                addFilter(sf);
            else
                throw new UnsupportedOperationException("Unknown filter");
        }

        sb.append('\n');
    }

    private void addFilter(UnaryFilter filter) {
        sb.append(getPropertyName(filter.property()))
            .append(" ")
            .append(operators.stringify(filter.operator()))
            // TODO Some sanitization should be done here.
            .append(" '")
            .append(filter.constant().value())
            .append("'");
    }

    private void addFilter(BinaryFilter filter) {
        sb.append(getPropertyName(filter.property1()))
            .append(operators.stringify(filter.operator()))
            .append(getPropertyName(filter.property2()));
    }

    private void addFilter(SetFilter filter) {
        sb.append(getPropertyName(filter.property()));

        final var values = filter.set();
        sb.append(' ')
            .append(operators.stringify(filter.operator()))
            .append(" [")
            .append(values.get(0));

        values.stream().skip(1).forEach(value -> sb.append(", ").append(value));

        sb.append(']');
    }

    private void addWith() {
        // For some reason joined ID variables are inserted as projections twice, so this band-aids the problem; a better fix might be to not make it happen in DatasourceTranslator or higher up (or it might not, idk)
        final HashSet<String> aliasedVars = new HashSet<>();

        sb.append("WITH\n  ");

        boolean first = true;

        for (final var p : projections) {
            if (aliasedVars.contains(p.structure().name)) continue;
            aliasedVars.add(p.structure().name);

            if (first) first = false; else sb.append(",\n  ");
            sb.append(getProjectionSrc(p))
                .append(" AS ")
                .append(getProjectionDst(p));
        }
    }

    private void addReturn() {
        sb.append("\nRETURN\n  ");

        final HashMap<String, ProjectionDst> resultStructure = new HashMap<>();
        for (final var p : projections) {
            var insertionMap = resultStructure;
            for (final var step : p.structure().getPathFromRoot()) {
                final var mid = insertionMap.computeIfAbsent(step.name, name -> ProjectionDst.createComplex());
                insertionMap = mid.subEntries;
            }

            insertionMap.put(p.structure().name, ProjectionDst.createSimple());
        }



        var first = true;
        for (final var entry : resultStructure.entrySet()) {
            if (first) first = false; else sb.append(",\n  ");

            if (!entry.getValue().isComplex()) {
                sb.append(escapeName(entry.getKey()));
            } else {
                entry.getValue().toProjection(sb);
                sb.append(" AS ").append(escapeName(entry.getKey()));
            }
        }
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

    private static @Nullable ComplexProperty findSubpathByPrefix(ComplexProperty path, String namePrefix) {
        for (final var subpath : path.subpaths()) {
            if (
                subpath.name() instanceof final StringName stringName
                && stringName.value.startsWith(namePrefix)
                && subpath instanceof final ComplexProperty complexSubpath
            )
                return complexSubpath;
        }

        return null;
    }

    private static @Nullable String findFromName(Mapping kind) {
        final var pfx = Neo4jControlWrapper.FROM_NODE_PROPERTY_PREFIX;
        return findSubpathByPrefix(kind.accessPath(), pfx)
            .name().toString().substring(pfx.length());
    }
    private static @Nullable String findToName(Mapping kind) {
        final var pfx = Neo4jControlWrapper.TO_NODE_PROPERTY_PREFIX;
        return findSubpathByPrefix(kind.accessPath(), pfx)
            .name().toString().substring(pfx.length());
    }

    private static String escapeName(String name) {
        return '`' + name + '`';
    }


    private static String mappingVarName(Mapping mapping) {
        return escapeName("VAR_" + mapping.kindName());
    }
    private static String mappingVarNameFrom(Mapping mapping) {
        return escapeName("VAR_" + findFromName(mapping));
    }
    private static String mappingVarNameTo(Mapping mapping) {
        return escapeName("VAR_" + findToName(mapping));
    }

    private void addNodeName(Mapping mapping) {
        sb.append('(')
            .append(mappingVarName(mapping))
            .append(':')
            .append(escapeName(mapping.kindName()))
            .append(')');
    }

    private void addRelationshipName(Mapping mapping) {
        sb.append('(')
            .append(mappingVarNameFrom(mapping))
            .append(")-[")
            .append(mappingVarName(mapping))
            .append(':')
            .append(escapeName(mapping.kindName()))
            .append("]->(")
            .append(mappingVarNameTo(mapping))
            .append(')');
    }


    private String getProjectionSrc(Projection projection) {
        // TODO if from or to then use relationship variables and push elsewhere
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
        final var propertyPath = property.mapping.accessPath().getPropertyPath(property.findFullPath());
        final var firstKey = propertyPath.get(0).name().toString();
        if (firstKey.startsWith(Neo4jControlWrapper.FROM_NODE_PROPERTY_PREFIX)) {
            return mappingVarNameFrom(property.mapping) + "." + propertyPath.stream().skip(1).map(ap -> escapeName(ap.name().toString())).collect(Collectors.joining("."));
        } else if (firstKey.startsWith(Neo4jControlWrapper.TO_NODE_PROPERTY_PREFIX)) {
            return mappingVarNameTo(property.mapping) + "." + propertyPath.stream().skip(1).map(ap -> escapeName(ap.name().toString())).collect(Collectors.joining("."));
        }

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

    private static class ProjectionDst {
        public final HashMap<String, ProjectionDst> subEntries;

        private ProjectionDst(boolean isComplex) {
            this.subEntries = isComplex ? new HashMap<>() : null;
        }
        public static ProjectionDst createSimple() {
            return new ProjectionDst(false);
        }
        public static ProjectionDst createComplex() {
            return new ProjectionDst(true);
        }
        public boolean isComplex() {
            return subEntries != null;
        }
        public void toProjection(StringBuilder sb) {
            assert isComplex();
            sb.append('{');
            boolean first = true;
            for (final var entry : subEntries.entrySet()) {
                if (first) first = false; else sb.append(", ");
                final var name = escapeName(entry.getKey());
                sb.append(name).append(": ");
                if (!entry.getValue().isComplex()) {
                    sb.append(name);
                } else {
                    entry.getValue().toProjection(sb);
                }
            }
            sb.append('}');
        }
    }

}
