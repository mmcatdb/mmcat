package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.wrapperjson.inference.RecordToHeuristicsMap;
import cz.matfyz.wrapperjson.inference.MapJsonDocument;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.bson.Document;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * An inference wrapper for JSON files that extends {@link AbstractInferenceWrapper}.
 * This class provides methods for loading and processing JSON data to infer schema descriptions
 * and properties using Spark RDDs.
 */
public class JsonInferenceWrapper extends AbstractInferenceWrapper {

    private final JsonProvider provider;

    /**
     * Constructs a new {@code JsonInferenceWrapper} with the specified JSON provider and Spark settings.
     */
    public JsonInferenceWrapper(JsonProvider provider, String kindName, SparkSettings sparkSettings) {
        super(kindName, sparkSettings);
        this.provider = provider;
    }

    /**
     * Loads record schema descriptions (RSDs) from the JSON data.
     */
    @Override public JavaRDD<RecordSchemaDescription> loadRSDs() {
        return loadDocuments().map(MapJsonDocument::process);
    }

    /**
     * Loads property schema pairs from the JSON data.
     * This method is currently not implemented.
     */
    @Override public JavaPairRDD<String, RecordSchemaDescription> loadPropertySchema() {
        throw new UnsupportedOperationException("Unimplemented method 'loadPropertySchema'");
    }

    /**
     * Loads property data from the JSON documents and maps them to {@link PropertyHeuristics}.
     */
    @Override public JavaRDD<PropertyHeuristics> loadPropertyData() {
        return loadDocuments().flatMap(new RecordToHeuristicsMap(kindName));
    }

    /**
     * Loads documents from the JSON file and parses them into a list of BSON {@link Document} documents. Parses both JSON file types - array of json documents as well as newline delimited documents.
     */
    public JavaRDD<ObjectNode> loadDocuments() {
        // It should be possible to do something like:
        // sparkSession.read().json(path);
        // There is even a config to switch between JSON lines and multiline JSON.
        // However, this doesn't work when the `path` is a URL. It should be possible to use HDFS or S3 or something like that, but that seems like too much work for now.

        try (
            InputStream inputStream = provider.getInputStream();
            Stream<ObjectNode> jsonStream = JsonParsedIterator.toStream(inputStream);
        ) {
            final var list = jsonStream.toList();
            return getContext().parallelize(list);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
