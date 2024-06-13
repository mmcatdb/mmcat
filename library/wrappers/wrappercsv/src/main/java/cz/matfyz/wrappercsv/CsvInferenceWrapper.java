package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.RawProperty;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Share;
import cz.matfyz.wrappercsv.inference.MapCsvDocument;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

public class CsvInferenceWrapper extends AbstractInferenceWrapper {

    private final CsvProvider provider;
    private final SparkSettings sparkSettings;

    private SparkSession sparkSession;
    private JavaSparkContext context;

    public CsvInferenceWrapper(CsvProvider provider, SparkSettings sparkSettings) {
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
    // assuming that the first line of the csv is the header, the csv is comma delimited and there are no missing data
    // TODO: get rid of assumptions
    public JavaRDD<RecordSchemaDescription> loadRSDs() {
        JavaRDD<Map<String, String>> csvDocuments = loadDocuments();
        return csvDocuments.map(MapCsvDocument::process);
    }

    public JavaRDD<Map<String, String>> loadDocuments() {
        JavaSparkContext newContext = new JavaSparkContext(sparkSession.sparkContext());
        newContext.setLogLevel("ERROR");
        newContext.setCheckpointDir(sparkSettings.checkpointDir());

        List<Map<String, String>> lines = new ArrayList<>();
        boolean firstLine = false;
        String[] header = null;

        try (InputStream inputStream = provider.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                char delimiter = detectDelimiter(line);
                String[] elements = line.split(Character.toString(delimiter) + "\\s*");
                if (!firstLine) {
                    header = elements;
                    firstLine = true;
                } else {
                    Map<String, String> lineMap = new HashMap<>();
                    for (int i = 0; i < elements.length; i++) {
                        lineMap.put(header[i], elements[i]);
                    }
                    lines.add(lineMap);
                }
            }
        } catch (IOException e) {
            System.err.println("Error processing input stream: " + e.getMessage());
            return context.emptyRDD();
        }
        JavaRDD<Map<String, String>> csvDocuments = context.parallelize(lines);
        return csvDocuments;
    }

    // The allowed delimiters: {',', '\t', ';'}
    private char detectDelimiter(String line) {
        char[] possibleDelimiters = {',', '\t', ';'};
        Map<Character, Integer> delimiterCount = new HashMap<>();
        for (char delimiter : possibleDelimiters) {
            int count = line.split(Character.toString(delimiter)).length - 1;
            delimiterCount.put(delimiter, count);
        }
        return Collections.max(delimiterCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    @Override
    public JavaPairRDD<String, RecordSchemaDescription> loadRSDPairs() {
        return null;

    }

    @Override
    public JavaPairRDD<String, RecordSchemaDescription> loadPropertySchema() {
        throw new UnsupportedOperationException("Unimplemented method 'loadPropertySchema'");
    }

    @Override
    public JavaPairRDD<String, PropertyHeuristics> loadPropertyData() {
        throw new UnsupportedOperationException("Unimplemented method 'loadPropertyData'");
    }

    @Override
    public AbstractInferenceWrapper copy() {
        return new CsvInferenceWrapper(
            this.provider,
            this.sparkSettings
        );
    }
}
