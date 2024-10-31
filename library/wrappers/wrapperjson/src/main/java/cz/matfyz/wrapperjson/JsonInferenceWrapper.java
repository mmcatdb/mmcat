package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.RawProperty;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Share;
import cz.matfyz.wrapperjson.inference.RecordToHeuristicsMap;
import cz.matfyz.wrapperjson.inference.MapJsonDocument;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.bson.Document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An inference wrapper for JSON files that extends {@link AbstractInferenceWrapper}.
 * This class provides methods for loading and processing JSON data to infer schema descriptions
 * and properties using Spark RDDs.
 */
public class JsonInferenceWrapper extends AbstractInferenceWrapper {

    private final JsonProvider provider;

    /**
     * Constructs a new {@code JsonInferenceWrapper} with the specified JSON provider and Spark settings.
     *
     * @param provider the JSON provider used to access JSON files.
     * @param sparkSettings the Spark settings used for configuring the Spark context.
     */
    public JsonInferenceWrapper(JsonProvider provider, SparkSettings sparkSettings) {
        super(sparkSettings);
        this.provider = provider;
    }

    /**
     * Returns the name of the JSON file currently being processed.
     *
     * @return the kind name as a string.
     */
    private String fileName() {
        return kindName;
    }

    /**
     * Creates a copy of this inference wrapper.
     *
     * @return a new instance of {@code JsonInferenceWrapper} with the same provider and Spark settings.
     */
    @Override public AbstractInferenceWrapper copy() {
        return new JsonInferenceWrapper(this.provider, this.sparkSettings);
    }

    /**
     * Loads properties from the JSON data. This method is currently not implemented.
     *
     * @param loadSchema a boolean indicating whether to load the schema.
     * @param loadData a boolean indicating whether to load the data.
     * @return null as this method is not implemented.
     */
    @Override public JavaPairRDD<RawProperty, Share> loadProperties(boolean loadSchema, boolean loadData) {
        return null;
    }

    /**
     * Loads record schema descriptions (RSDs) from the JSON data.
     *
     * @return a {@link JavaRDD} of {@link RecordSchemaDescription} objects.
     */
    @Override public JavaRDD<RecordSchemaDescription> loadRSDs() {
        JavaRDD<Document> jsonDocuments = loadDocuments();
        return jsonDocuments.map(MapJsonDocument::process);
    }

    /**
     * Loads documents from the JSON file and parses them into a list of BSON {@link Document} objects.
     *
     * @return a {@link JavaRDD} of BSON {@link Document} objects representing JSON data.
     */
    public JavaRDD<Document> loadDocuments() {
        List<Document> documents = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try (
            InputStream inputStream = provider.getInputStream(kindName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            String content = reader.lines().collect(Collectors.joining("\n"));
            try {
                JsonNode jsonNode = objectMapper.readTree(content);
                if (jsonNode.isArray()) {
                    for (JsonNode node : jsonNode) {
                        documents.add(Document.parse(node.toString()));
                    }
                } else {
                    documents.add(Document.parse(jsonNode.toString()));
                }
            } catch (IOException e) {
                reader.lines().forEach(line -> {
                    try {
                        documents.add(Document.parse(line));
                    } catch (Exception ex) {
                        System.err.println("Error parsing line as JSON: " + ex.getMessage());
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Error processing input stream: " + e.getMessage());
            return context.emptyRDD();
        }
        JavaRDD<Document> jsonDocuments = context.parallelize(documents);
        return jsonDocuments;
    }

    /**
     * Loads pairs of strings and record schema descriptions (RSDs) from the JSON data.
     * This method is currently not implemented.
     *
     * @return null as this method is not implemented.
     */
    @Override public JavaPairRDD<String, RecordSchemaDescription> loadRSDPairs() {
        return null;
    }

    /**
     * Loads property schema pairs from the JSON data. This method is currently not implemented.
     *
     * @return nothing, as this method always throws an exception.
     * @throws UnsupportedOperationException always thrown as this method is not implemented.
     */
    @Override public JavaPairRDD<String, RecordSchemaDescription> loadPropertySchema() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadPropertySchema'");
    }

    /**
     * Loads property data from the JSON documents and maps them to {@link PropertyHeuristics}.
     *
     * @return a {@link JavaPairRDD} of string keys and {@link PropertyHeuristics} objects.
     */
    @Override public JavaPairRDD<String, PropertyHeuristics> loadPropertyData() {
        JavaRDD<Document> jsonDocuments = loadDocuments();

        return jsonDocuments.flatMapToPair(new RecordToHeuristicsMap(fileName()));
    }

    /**
     * Retrieves a list of kind names (JSON file names) from the provider.
     *
     * @return a list of JSON file names as strings.
     * @throws RuntimeException if an error occurs while retrieving the file names.
     */
    @Override public List<String> getKindNames() {
        try {
            return provider.getJsonFileNames();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("Error getting jsonc file names", e);
        }
    }

}
