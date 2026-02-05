package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractEvolutionWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;

/**
 * MongoDB implementation of evolution wrapper.
 * Generates MongoDB commands (updateMany with aggregation pipeline, drop, createCollection)
 * to propagate schema evolution operations to MongoDB databases.
 *
 * Key design decisions for MongoDB:
 * - Schemaless: AddProperty is no-op (fields created on first write)
 * - GROUP uses native document nesting via aggregation pipeline $set
 * - JOIN uses $concat aggregation operator
 * - SPLIT uses $split + $arrayElemAt aggregation operators
 * - Commands are generated as BsonDocument for driver execution
 */
public class MongoDBEvolutionWrapper implements AbstractEvolutionWrapper {

    @Override
    public List<AbstractStatement> evolveCreateObjex(String kindName, List<PropertyInfo> properties) {
        // Create the collection (properties are irrelevant for schemaless MongoDB)
        final var command = new BsonDocument("create", new BsonString(kindName));
        final var content = "db.createCollection(\"" + kindName + "\");";
        return List.of(new MongoDBCommandStatement(content, command));
    }

    @Override
    public List<AbstractStatement> evolveDeleteObjex(String kindName) {
        final var command = new BsonDocument("drop", new BsonString(kindName));
        final var content = "db." + kindName + ".drop();";
        return List.of(new MongoDBCommandStatement(content, command));
    }

    @Override
    public List<AbstractStatement> evolveAddProperty(String kindName, String propertyName, boolean isRequired) {
        // MongoDB is schemaless — fields are created on first write.
        // No migration needed for adding a property.
        return List.of();
    }

    @Override
    public List<AbstractStatement> evolveRemoveProperty(String kindName, String propertyName) {
        // Use updateMany with $unset to remove the field from all documents
        final var pipeline = new BsonArray();
        pipeline.add(new BsonDocument("$unset", new BsonString(propertyName)));

        return List.of(createUpdateManyCommand(
            kindName,
            pipeline,
            "db." + kindName + ".updateMany({}, [{$unset: \"" + propertyName + "\"}]);"
        ));
    }

    @Override
    public List<AbstractStatement> evolveGroupProperties(String kindName, String groupName, List<String> propertyNames) {
        // MongoDB aggregation pipeline: $set the group as nested doc, $unset the originals
        // db.collection.updateMany({}, [
        //   {$set: {"group": {"p1": "$p1", "p2": "$p2"}}},
        //   {$unset: ["p1", "p2"]}
        // ])

        final var pipeline = new BsonArray();

        // $set: create nested document
        final var groupDoc = new BsonDocument();
        for (final String prop : propertyNames) {
            groupDoc.append(prop, new BsonString("$" + prop));
        }
        pipeline.add(new BsonDocument("$set", new BsonDocument(groupName, groupDoc)));

        // $unset: remove original top-level fields
        final var unsetArray = new BsonArray();
        for (final String prop : propertyNames) {
            unsetArray.add(new BsonString(prop));
        }
        pipeline.add(new BsonDocument("$unset", unsetArray));

        final String propsStr = propertyNames.stream()
            .map(p -> "\"" + p + "\": \"$" + p + "\"")
            .collect(Collectors.joining(", "));
        final String unsetStr = propertyNames.stream()
            .map(p -> "\"" + p + "\"")
            .collect(Collectors.joining(", "));
        final String content = "db." + kindName + ".updateMany({}, [{$set: {\"" + groupName + "\": {" + propsStr + "}}}, {$unset: [" + unsetStr + "]}]);";

        return List.of(createUpdateManyCommand(kindName, pipeline, content));
    }

    @Override
    public List<AbstractStatement> evolveUngroupProperty(String kindName, String groupName, List<String> propertyNames) {
        // Inverse of GROUP: $set top-level fields from nested, $unset the group
        // db.collection.updateMany({}, [
        //   {$set: {"p1": "$group.p1", "p2": "$group.p2"}},
        //   {$unset: "group"}
        // ])

        final var pipeline = new BsonArray();

        // $set: extract properties from group to top level
        final var setDoc = new BsonDocument();
        for (final String prop : propertyNames) {
            setDoc.append(prop, new BsonString("$" + groupName + "." + prop));
        }
        pipeline.add(new BsonDocument("$set", setDoc));

        // $unset: remove the group field
        pipeline.add(new BsonDocument("$unset", new BsonString(groupName)));

        final String setStr = propertyNames.stream()
            .map(p -> "\"" + p + "\": \"$" + groupName + "." + p + "\"")
            .collect(Collectors.joining(", "));
        final String content = "db." + kindName + ".updateMany({}, [{$set: {" + setStr + "}}, {$unset: \"" + groupName + "\"}]);";

        return List.of(createUpdateManyCommand(kindName, pipeline, content));
    }

    @Override
    public List<AbstractStatement> evolveJoinProperties(
        String kindName, String targetProperty,
        List<String> sourceProperties, String delimiter,
        boolean deleteSources
    ) {
        // db.collection.updateMany({}, [
        //   {$set: {"target": {$concat: ["$c1", "delim", "$c2"]}}},
        //   {$unset: ["c1", "c2"]}  // optional
        // ])

        final var pipeline = new BsonArray();

        // Build $concat array: ["$c1", "delim", "$c2", "delim", "$c3"]
        final var concatArray = new BsonArray();
        for (int i = 0; i < sourceProperties.size(); i++) {
            if (i > 0) {
                concatArray.add(new BsonString(delimiter));
            }
            concatArray.add(new BsonString("$" + sourceProperties.get(i)));
        }

        final var concatExpr = new BsonDocument("$concat", concatArray);
        pipeline.add(new BsonDocument("$set", new BsonDocument(targetProperty, concatExpr)));

        // Optionally unset source fields
        if (deleteSources) {
            final var unsetArray = new BsonArray();
            for (final String src : sourceProperties) {
                unsetArray.add(new BsonString(src));
            }
            pipeline.add(new BsonDocument("$unset", unsetArray));
        }

        final String srcStr = sourceProperties.stream()
            .map(p -> "\"$" + p + "\"")
            .collect(Collectors.joining(", \"" + delimiter + "\", "));
        final String content = "db." + kindName + ".updateMany({}, [{$set: {\"" + targetProperty + "\": {$concat: [" + srcStr + "]}}}]);";

        return List.of(createUpdateManyCommand(kindName, pipeline, content));
    }

    @Override
    public List<AbstractStatement> evolveSplitProperty(
        String kindName, String sourceProperty,
        List<String> targetProperties, String delimiter,
        boolean deleteSource
    ) {
        // db.collection.updateMany({}, [
        //   {$set: {
        //     "c1": {$arrayElemAt: [{$split: ["$source", "delim"]}, 0]},
        //     "c2": {$arrayElemAt: [{$split: ["$source", "delim"]}, 1]}
        //   }},
        //   {$unset: "source"}  // optional
        // ])

        final var pipeline = new BsonArray();

        // Build the $split expression (shared across all targets)
        final var splitExpr = new BsonArray();
        splitExpr.add(new BsonString("$" + sourceProperty));
        splitExpr.add(new BsonString(delimiter));
        final var splitDoc = new BsonDocument("$split", splitExpr);

        // Build $set with $arrayElemAt for each target
        final var setDoc = new BsonDocument();
        for (int i = 0; i < targetProperties.size(); i++) {
            final var elemAtArray = new BsonArray();
            elemAtArray.add(splitDoc);
            elemAtArray.add(new BsonInt32(i));
            setDoc.append(targetProperties.get(i), new BsonDocument("$arrayElemAt", elemAtArray));
        }
        pipeline.add(new BsonDocument("$set", setDoc));

        // Optionally unset source
        if (deleteSource) {
            pipeline.add(new BsonDocument("$unset", new BsonString(sourceProperty)));
        }

        final String content = "db." + kindName + ".updateMany({}, [{$set: {" +
            java.util.stream.IntStream.range(0, targetProperties.size())
                .mapToObj(i -> "\"" + targetProperties.get(i) + "\": {$arrayElemAt: [{$split: [\"$" + sourceProperty + "\", \"" + delimiter + "\"]}, " + i + "]}")
                .collect(Collectors.joining(", ")) +
            "}}]);";

        return List.of(createUpdateManyCommand(kindName, pipeline, content));
    }

    @Override
    public List<AbstractStatement> evolveMoveProperty(
        String sourceKindName, String sourceProperty,
        String targetKindName, String targetProperty
    ) {
        final var statements = new ArrayList<AbstractStatement>();

        if (sourceKindName.equals(targetKindName)) {
            // Same collection: use $rename
            final var renameDoc = new BsonDocument(sourceProperty, new BsonString(targetProperty));

            final var command = new BsonDocument()
                .append("update", new BsonString(sourceKindName))
                .append("updates", new BsonArray(List.of(
                    new BsonDocument()
                        .append("q", new BsonDocument())
                        .append("u", new BsonDocument("$rename", renameDoc))
                        .append("multi", new BsonBoolean(true))
                )));

            statements.add(new MongoDBCommandStatement(
                "db." + sourceKindName + ".updateMany({}, {$rename: {\"" + sourceProperty + "\": \"" + targetProperty + "\"}});",
                command
            ));
        } else {
            // Cross-collection move is complex — use aggregation $merge or manual approach
            // For simplicity, generate two commands:
            // 1. Aggregate source docs and merge the property into target
            // 2. Unset the property from source

            // Step 1: This requires application-level logic or $lookup.
            // For now, generate a $unset on source as a minimal step.
            // Full cross-collection migration would need the EvolutionPropagator to coordinate.
            final var unsetPipeline = new BsonArray();
            unsetPipeline.add(new BsonDocument("$unset", new BsonString(sourceProperty)));

            statements.add(createUpdateManyCommand(
                sourceKindName,
                unsetPipeline,
                "db." + sourceKindName + ".updateMany({}, [{$unset: \"" + sourceProperty + "\"}]);"
            ));
        }

        return statements;
    }

    /**
     * Helper to create a MongoDB updateMany command with an aggregation pipeline.
     * Uses the MongoDB `update` command format with multi:true.
     */
    private MongoDBCommandStatement createUpdateManyCommand(String collectionName, BsonArray pipeline, String humanReadable) {
        final var command = new BsonDocument()
            .append("update", new BsonString(collectionName))
            .append("updates", new BsonArray(List.of(
                new BsonDocument()
                    .append("q", new BsonDocument())
                    .append("u", pipeline)
                    .append("multi", new BsonBoolean(true))
            )));

        return new MongoDBCommandStatement(humanReadable, command);
    }

}
