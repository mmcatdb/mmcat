package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.querycontent.QueryContent;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.conversions.Bson;
import org.bson.json.JsonWriterSettings;

// There are two types of mongodb quries - database commands and mongosh methods.
// The commands are run by `db.runCommand({ ... })`. For example, `db.runCommand({ find: 'collection', filter: { ... }, ... })`. The output is always a document (usually containing an array of found documents).
// The mongosh methods are run by `db.collection.method(...)`. For example, `db.order.find({ ... })`. The output is always a cursor.
//
// In java, the mongosh methods are mapped to the driver methods, meaning that it generally isn't possible to select which method will be used by some string parameter (unless we use reflection or a huge switch). So the commands seems to be better.
// However, for querying, we only want to use a very limited subset of the commands. Specifically, the `aggregate` method should cover all the queries we want to run. It has filters, projections, and, of course, aggregations.
// The benefit of using the `aggregate` method is that it returns a cursor, which is exactly what we want. On the contrary, the command version of aggregation returns document which even doesn't have a stable structure (it can change between releases).
// Therefore, in our usecase, the aggregation method seems like the best choice.

/**
 * Represents an aggregate query that will be run against the MongoDB driver.
 */
public class MongoDBQuery implements QueryContent {

    public final String collection;
    public final List<Bson> pipeline;

    public MongoDBQuery(String collection, List<Bson> pipeline) {
        this.collection = collection;
        this.pipeline = pipeline;
    }

    /**
     * The most simple query - finds all documents in the collection.
     */
    public static MongoDBQuery findAll(String collection) {
        return new MongoDBQuery(collection, List.of());
    }

    @Override public String toString() {
        final JsonWriterSettings settings = JsonWriterSettings.builder()
            .indent(true)
            .indentCharacters("    ")
            .build();

        final String pipelineString = pipeline.stream().map(item -> item.toBsonDocument().toJson(settings)).collect(Collectors.joining(", "));

        return "db." + collection + ".aggregate([ " + pipelineString + " ])";
    }

}
