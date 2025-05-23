package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.utils.BaseQueryWrapper;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.Computation.Operator;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.core.mapping.Name.StringName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mongodb.client.model.Aggregates;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.conversions.Bson;

@SuppressWarnings("java:S125")
public class MongoDBQueryWrapper extends BaseQueryWrapper implements AbstractQueryWrapper {

    // CHECKSTYLE:OFF
    @Override public boolean isJoinSupported() { return false; }
    @Override public boolean isOptionalJoinSupported() { return false; }
    @Override public boolean isRecursiveJoinSupported() { return false; }
    @Override public boolean isFilteringSupported() { return true; }
    @Override public boolean isFilteringNotIndexedSupported() { return true; }
    @Override public boolean isAggregationSupported() { return true; }
    // CHECKSTYLE:ON

    private static final Operators operators = new Operators();

    static {

        operators.define(Operator.Equal, "$eq");
        operators.define(Operator.NotEqual, "$ne");
        operators.define(Operator.Less, "$lt");
        operators.define(Operator.LessOrEqual, "$lte");
        operators.define(Operator.Greater, "$gt");
        operators.define(Operator.GreaterOrEqual, "$gte");

        // TODO aggregation operators

        operators.define(Operator.In, "$in");
        operators.define(Operator.NotIn, "$nin");

    }

    public QueryStatement createDSLStatement() {
        // Mongo doesn't allow joins so there is only one mapping.
        final Mapping mapping = projections.getFirst().property().mapping;
        final String collectionName = mapping.kindName();
        final var pipeline = new ArrayList<Bson>();

        final var filtersDocument = new BsonDocument();
        for (final var filter : filters)
            addFilter(filtersDocument, filter);
        pipeline.add(Aggregates.match(filtersDocument));

        final Bson projection = createProjections();
        pipeline.add(Aggregates.project(projection));

        final var content = new MongoDBQuery(collectionName, pipeline);

        return new QueryStatement(content, context.rootStructure());
    }

    private void addFilter(BsonDocument filtersDocument, Filter filter) {
        if (filter instanceof BinaryFilter)
            throw new UnsupportedOperationException("Mongo does not support filters comparing two variables.");

        Property property;
        final var filterCondition = new BsonDocument();

        if (filter instanceof UnaryFilter unaryFilter) {
            property = unaryFilter.property();

            final var constant = new BsonString(unaryFilter.constant().value());

            final var operator = operators.stringify(unaryFilter.operator());
            filterCondition.put(operator, constant);
        }
        else if (filter instanceof SetFilter setFilter) {
            property = setFilter.property();

            final var constants = new BsonArray();
            for (final var constant : setFilter.set())
            constants.add(new BsonString(constant.value()));

            final var operator = operators.stringify(setFilter.operator());
            filterCondition.put(operator, constants);
        }
        else {
            throw new UnsupportedOperationException("Mongo does not support filter type \"" + filter.getClass().getSimpleName() + "\".");
        }

        final var propertyPath = getPropertyName(property);
        filtersDocument.put(propertyPath, filterCondition);
    }

    private Bson createProjections() {
        final var output = new BsonDocument();
        for (final var projection : projections)
            Projector.createProjection(context, output, projection);

        return output;
    }

    private static String getPropertyName(Property property) {
        return property.mapping.accessPath().getPropertyPath(property.path).stream().map(accPath -> {
            final var name = accPath.name();
            if (!(name instanceof StringName stringName))
                throw new UnsupportedOperationException("Only string names are supported.");

            return stringName.value;
        }).collect(Collectors.joining("."));
    }

    // This class is a little more complicated than it seems. The problem is that we have to match the result structure of the projection to the access path of the property. The matching points are the array result structures.
    // The reason is that we have to use a special mongo syntax for them (see the example below). Therefore, we can't just use the whole access path from the root to the property. We have to split it by the array result structures.
    // The complications came from the two facts:
    //  - The access path can have more properties than the result structure (because of auxiliary properties).
    //      - This should be ok since we jsut traverse them as we go.
    //  - The access path can have less properties than the result structure (because it can use composite signatures).
    private static class Projector {

        public static void createProjection(AbstractWrapperContext context, BsonDocument root, Projection projection) {
            new Projector(context, root).run(projection);
        }

        AbstractWrapperContext context;
        private BsonDocument currentDocument;

        private Projector(AbstractWrapperContext context, BsonDocument root) {
            this.context = context;
            currentDocument = root;
        }

        private void run(Projection projection) {
            lastAccessPath = projection.property().mapping.accessPath();
            final ResultStructure structure = projection.structure();

            for (final ResultStructure step : structure.getPathFromRoot()) {
                traverseAccessPath(step);
                traverseStructure(step);
            }

            traverseAccessPath(structure);

            // Now we can finish the projection.
            currentDocument.put(structure.name, new BsonString("$" + createNameInMongo()));
        }

        /** If we are in the map, we will need to use the $$this variable in the $map function. */
        private boolean isInMap = false;

        private void traverseStructure(ResultStructure structure) {
            // If it isn't array, we just continue to the document (or create a new one).
            if (!structure.isArray) {
                traverseSimpleStructure(structure);
                return;
            }

            // It's an array. We have to use the $map function in order to map the objects correctly. See the example below.
            if (currentDocument.containsKey(structure.name)) {
                currentDocument = currentDocument.getDocument(structure.name).getDocument("$map").getDocument("in");
                emptyLastPathBuffer();
            }
            else {
                currentDocument = createMapOperator(currentDocument, structure.name, createNameInMongo());
            }

            isInMap = true;
        }

        private void traverseSimpleStructure(ResultStructure structure) {
            if (currentDocument.containsKey(structure.name)) {
                currentDocument = currentDocument.getDocument(structure.name);
                return;
            }

            final var child = new BsonDocument();
            currentDocument.put(structure.name, child);
            currentDocument = child;
        }

        private static BsonDocument createMapOperator(BsonDocument parent, String structureName, String pathName) {
            final var nested = new BsonDocument();
            parent.put(structureName, nested);

            final var map = new BsonDocument();
            nested.put("$map", map);

            map.put("input", new BsonString("$" + pathName));
            final var child = new BsonDocument();
            map.put("in", child);

            return child;
        }

        /** Path since the last split by the last array result structure. */
        private AccessPath lastAccessPath;
        /** Signature from the lastAccessPath to the current object. */
        private Signature fromLastPath = Signature.createEmpty();

        private void traverseAccessPath(ResultStructure structure) {
            final var property = context.getProperty(structure);
            fromLastPath = fromLastPath.concatenate(property.path);
        }

        /** Collects the access paths since the last split. Also resets the signature path and sets the new lastAccessPath. */
        private List<AccessPath> emptyLastPathBuffer() {
            if (!(lastAccessPath instanceof ComplexProperty lastComplex))
                throw new UnsupportedOperationException("Can't traverse simple access path.");

            final List<AccessPath> accessPaths = lastComplex.getPropertyPath(fromLastPath);
            lastAccessPath = accessPaths.getLast();
            fromLastPath = Signature.createEmpty();

            return accessPaths;
        }

        /** Returns a name in mongo that references the current object. */
        private String createNameInMongo() {
            final String output = emptyLastPathBuffer().stream()
                .map(accessPath -> {
                    if (!(accessPath.name() instanceof StringName stringName))
                        throw new UnsupportedOperationException("Only static names are supported.");

                    return stringName.value;
                })
                .collect(Collectors.joining("."));

            // If we are in the $map function, we can use "$$this" variable to reference the current object. So it's like if the object's name was "$this". The thing is that once we are in a map, we are always in a map (either in the original or in its children).
            // We tested that this works even with nested maps (i.e., the "$$this" variable can be reused). It should be also always valid (unless someone uses "$this" as a field name in the access path, but that would be just so unbelievably stupid.
            return isInMap
                ? "$this." + output
                : output;
        }

    }

    // Array mapping example. Let's consider the following mongodb collection:
    //  [ {
    //      a: [ { b: "b1", c: "c1" } ]
    //  } ]
    //
    // Now, let's run this query:
    //  { "$project": {
    //      "a.b": 1,
    //      "a.c": 1,
    //      "x": { "y": "$a.b", "z": "$a.c" }
    //  } }
    //
    // The result will be (we omit the _id field for simplicity):
    //  [ {
    //      "a": [ { "b": "b1", "c": "c1" } ],
    //      "x": { "y": [ "b1" ], "z": [ "c1" ] }
    //  } ]
    //
    // As we can see, it's easy to get the original structure. However, one can't simple rename the properties it he we wants to keep the cardinality. Sure, we can rename the non-array properties and their structure will be preserved, but not if they are in arrays.
    // In order to fix this, we have to use the $map function. The query will look like this:
    //  { "$project": {
    //      "a.b": 1,
    //      "a.c": 1,
    //      "x": { "$map": { "input": "$a", "in": {
    //          "y": "$$this.b",
    //          "z": "$$this.c"
    //      } } }
    // } }
    //
    // In some cases, this approach isn't necessary. If there was only one field in the array's objects, we wouldn't have to do this at all. But it's easier to implement only one use case than two.
    // There is also a question about the performance. Because we can use the simple projection and then transform the data in the application. But we would have to test it on large data to really see the difference. Until then, this is good enough.

}
