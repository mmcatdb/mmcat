package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.RawProperty;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Share;
import cz.matfyz.wrapperjson.inference.DocumentToHeuristicsMap;
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
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.bson.Document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonInferenceWrapper extends AbstractInferenceWrapper {

    private final JsonProvider provider;
    private final SparkSettings sparkSettings;

    private SparkSession sparkSession;
    private JavaSparkContext context;

    private String fileName() {
        return kindName;
    }

    public JsonInferenceWrapper(JsonProvider provider, SparkSettings sparkSettings) {
        this.provider = provider;
        this.sparkSettings = sparkSettings;
    }

    @Override
    public void buildSession() {
        sparkSession = SparkSession.builder().master(sparkSettings.master())
            .getOrCreate();
        context = new JavaSparkContext(sparkSession.sparkContext());
        context.setLogLevel("ERROR");
    }

    @Override
    public void stopSession() {
            sparkSession.stop();
    }

    @Override
    public void initiateContext() {
        context = new JavaSparkContext(sparkSession.sparkContext());
        context.setLogLevel("ERROR");
    }

    @Override
    public JavaPairRDD<RawProperty, Share> loadProperties(boolean loadSchema, boolean loadData) {
        return null;
    }

    @Override
    public JavaRDD<RecordSchemaDescription> loadRSDs() {
        JavaRDD<Document> jsonDocuments = loadDocuments();
        return jsonDocuments.map(MapJsonDocument::process);
    }

    public JavaRDD<Document> loadDocuments() {
        JavaSparkContext newContext = new JavaSparkContext(sparkSession.sparkContext());
        newContext.setLogLevel("ERROR");
        newContext.setCheckpointDir(sparkSettings.checkpointDir());

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

    @Override
    public JavaPairRDD<String, RecordSchemaDescription> loadRSDPairs() {
        return null;

    }

     @Override
    public JavaPairRDD<String, RecordSchemaDescription> loadPropertySchema() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadPropertySchema'");
    }

    @Override
    public JavaPairRDD<String, PropertyHeuristics> loadPropertyData() {
        JavaRDD<Document> jsonDocuments = loadDocuments();

        return jsonDocuments.flatMapToPair(new DocumentToHeuristicsMap(fileName()));
    }

    @Override
    public List<String> getKindNames() {
        try {
            return provider.getJsonFileNames();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error getting json file names", e);
        }
    }

    @Override
    public AbstractInferenceWrapper copy() {
        return new JsonInferenceWrapper(
            this.provider,
            this.sparkSettings
        );
    }
}
