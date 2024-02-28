package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.utils.BaseQueryWrapper;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.StaticName;
import cz.matfyz.core.querying.QueryStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.mongodb.client.model.Aggregates;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.conversions.Bson;

public class MongoDBQueryWrapper extends BaseQueryWrapper implements AbstractQueryWrapper {

    // CHECKSTYLE:OFF
    @Override public boolean isJoinSupported() { return false; }
    @Override public boolean isOptionalJoinSupported() { return false; }
    @Override public boolean isRecursiveJoinSupported() { return false; }
    @Override public boolean isFilteringSupported() { return true; }
    @Override public boolean isFilteringNotIndexedSupported() { return true; }
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
        final Bson projection = createProjections();
        final var pipeline = List.of(
            Aggregates.project(projection)
        );
        System.out.println(projection);

        final var content = new MongoDBQuery(collectionName, pipeline);

        return new QueryStatement(content, rootStructure);
    }

    private final Bson createProjections() {
        final var output = new BsonDocument();
        for (final var projection : projections)
            createProjection(output, projection);

        return output;
    }

    private void createProjection(BsonDocument root, Projection projection) {
        final var path = getParentPath(projection.structure());
        BsonDocument current = root;

        for (final var step : path) {
            if (current.containsKey(step.name)) {
                current = current.getDocument(step.name);
            }
            else {
                final var child = new BsonDocument();
                current.put(step.name, child);
                current = child;
            }
        }

        final List<AccessPath> accessPaths = projection.property().findFullAccessPath();
        if (accessPaths == null)
            throw new UnsupportedOperationException("Access path not found");

        final String pathString = accessPaths.stream().map(accessPath -> {
            if (!(accessPath.name() instanceof StaticName staticName))
                throw new UnsupportedOperationException("Only static names are supported.");

            return staticName.getStringName();
        }).collect(Collectors.joining("."));

        current.put(projection.structure().name, new BsonString("$" + pathString));
    }

    /** Returns the path from the root (not included) of the structure all the way to the input structure (also not included). */
    private List<QueryStructure> getParentPath(QueryStructure structure) {
        List<QueryStructure> path = new ArrayList<>();
        while (structure.parent() != null) {
            path.add(structure.parent());
            structure = structure.parent();
        }
        if (!path.isEmpty())
            path.removeLast();

        return path.reversed();
    }

}
