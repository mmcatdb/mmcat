package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.utils.BaseQueryWrapper;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.querying.QueryStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MongoDBQueryWrapper extends BaseQueryWrapper implements AbstractQueryWrapper {

    // CHECKSTYLE:OFF
    @Override public boolean isJoinSupported() { return false; }
    @Override public boolean isOptionalJoinSupported() { return false; }
    @Override public boolean isRecursiveJoinSupported() { return false; }
    @Override public boolean isFilteringSupported() { return true; }
    @Override public boolean IsFilteringNotIndexedSupported() { return true; }
    @Override public boolean isAggregationSupported() { return true; }
    // CHECKSTYLE:ON

    @Override protected Map<ComparisonOperator, String> defineComparisonOperators() {
        final var output = new TreeMap<ComparisonOperator, String>();
        output.put(ComparisonOperator.Equal, "$eq");
        output.put(ComparisonOperator.NotEqual, "$ne");
        output.put(ComparisonOperator.Less, "$lt");
        output.put(ComparisonOperator.LessOrEqual, "$lte");
        output.put(ComparisonOperator.Greater, "$gt");
        output.put(ComparisonOperator.GreaterOrEqual, "$gte");
        return output;
    }

    @Override protected Map<AggregationOperator, String> defineAggregationOperators() {
        // TODO fix
        return new TreeMap<>();
    }

    public QueryStatement createDSLStatement() {
        // Mongo doesn't allow joins so there is only one mapping.
        final Mapping mapping = projections.getFirst().property().kind.mapping;
        final String collectionName = mapping.kindName();
        final var content = MongoDBQuery.findAll(collectionName);

        final var root = new QueryStructure(rootIdentifier, true);

        // projections.forEach();

        // TODO got one projection "v_street" with path "8.9"

        return new QueryStatement(content, root);
    }

    private void addProjectionToStructure(QueryStructure structure, Projection projection) {
        // projection.
        
    }

    /** The `path` field replaces the `property.path` of the `projection`.  */
    private record FixedProjection(Projection projection, Signature path) {}

    /**
     * This functions get all projections (or at least those that are relevant for this part of the access path). Then it recursively transforms them to the query structure.
     * The projections are leaves, the final structure is a tree.
     */
    private void addPathToStructure(QueryStructure parentStructure, ComplexProperty parentPath, List<FixedProjection> projections) {
        // if (!(accessPath instanceof ComplexProperty complexPath)) {

        //     final var simplePath = (SimpleProperty) accessPath;
        //     parentStructure.addChild(new QueryStructure(p.identifier(), false))
        //     return;
        //     // TODO
        // }

        for (final var child : parentPath.subpaths()) {
            final var newProjections = traverseProjections(projections, child.signature());

            if (newProjections.isEmpty())
                continue;

            if (newProjections.size() == 1) {
                
            }

        }
    }

    /**
     * Remove the `signature` from the start of the `path` of each projection. If the `signature` is not a prefix of the `path`, the projection is removed.
     */
    private List<FixedProjection> traverseProjections(List<FixedProjection> projections, Signature signature) {
        final List<FixedProjection> output = new ArrayList<>();
        for (final var projection : projections) {
            final Signature newPath = projection.path.cutPrefix(signature);
            if (newPath == null)
                continue;

            output.add(new FixedProjection(projection.projection, newPath));
        }

        return output;
    }



}