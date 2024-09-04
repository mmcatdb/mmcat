package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.RawProperty;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Share;
import cz.matfyz.wrappercsv.inference.RecordToHeuristicsMap;
import cz.matfyz.wrappercsv.inference.MapCsvDocument;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

/**
 * An inference wrapper for CSV files that extends {@link AbstractInferenceWrapper}.
 * This class provides methods for loading and processing CSV data to infer schema descriptions
 * and properties using Spark RDDs.
 */
public class CsvInferenceWrapper extends AbstractInferenceWrapper {

    private final CsvProvider provider;

    /**
     * Constructs a new {@code CsvInferenceWrapper} with the specified CSV provider and Spark settings.
     *
     * @param provider the CSV provider used to access CSV files.
     * @param sparkSettings the Spark settings used for configuring the Spark context.
     */
    public CsvInferenceWrapper(CsvProvider provider, SparkSettings sparkSettings) {
        super(sparkSettings);
        this.provider = provider;
    }

    /**
     * Returns the name of the CSV file currently being processed.
     *
     * @return the kind name as a string.
     */
    private String fileName() {
        return kindName;
    }

    /**
     * Creates a copy of this inference wrapper.
     *
     * @return a new instance of {@code CsvInferenceWrapper} with the same provider and Spark settings.
     */
    @Override
    public AbstractInferenceWrapper copy() {
        return new CsvInferenceWrapper(this.provider, this.sparkSettings);
    }

    /**
     * Loads properties from the CSV data. This method is currently not implemented.
     *
     * @param loadSchema a boolean indicating whether to load the schema.
     * @param loadData a boolean indicating whether to load the data.
     * @return null as this method is not implemented.
     */
    @Override
    public JavaPairRDD<RawProperty, Share> loadProperties(boolean loadSchema, boolean loadData) {
        return null;
    }

    /**
     * Loads record schema descriptions (RSDs) from the CSV data.
     * Assumes the first line of the CSV is the header, the CSV is comma-delimited,
     * and there are no missing data.
     *
     * TODO: get rid of assumptions.
     *
     * @return a {@link JavaRDD} of {@link RecordSchemaDescription} objects.
     */
    @Override
    public JavaRDD<RecordSchemaDescription> loadRSDs() {
        JavaRDD<Map<String, String>> csvDocuments = loadDocuments();
        return csvDocuments.map(MapCsvDocument::process);
    }

    /**
     * Loads documents from the CSV file and parses them into a list of maps,
     * where each map represents a CSV row with header names as keys.
     *
     * @return a {@link JavaRDD} of maps containing CSV row data.
     */
    public JavaRDD<Map<String, String>> loadDocuments() {
        List<Map<String, String>> lines = new ArrayList<>();
        boolean firstLine = false;
        String[] header = null;

        try (InputStream inputStream = provider.getInputStream(kindName);
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

    /**
     * Detects the delimiter used in the CSV line. The allowed delimiters are ',', '\t', and ';'.
     *
     * @param line the line from the CSV file to analyze.
     * @return the detected delimiter character.
     */
    private char detectDelimiter(String line) {
        char[] possibleDelimiters = {',', '\t', ';'};
        Map<Character, Integer> delimiterCount = new HashMap<>();
        for (char delimiter : possibleDelimiters) {
            int count = line.split(Character.toString(delimiter)).length - 1;
            delimiterCount.put(delimiter, count);
        }
        return Collections.max(delimiterCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    /**
     * Loads pairs of strings and record schema descriptions (RSDs) from the CSV data.
     * This method is currently not implemented.
     *
     * @return null as this method is not implemented.
     */
    @Override
    public JavaPairRDD<String, RecordSchemaDescription> loadRSDPairs() {
        return null;
    }

    /**
     * Loads property schema pairs from the CSV data. This method is currently not implemented.
     *
     * @return nothing, as this method always throws an exception.
     * @throws UnsupportedOperationException always thrown as this method is not implemented.
     */
    @Override
    public JavaPairRDD<String, RecordSchemaDescription> loadPropertySchema() {
        throw new UnsupportedOperationException("Unimplemented method 'loadPropertySchema'");
    }

    /**
     * Loads property data from the CSV documents and maps them to {@link PropertyHeuristics}.
     *
     * @return a {@link JavaPairRDD} of string keys and {@link PropertyHeuristics} objects.
     */
    @Override
    public JavaPairRDD<String, PropertyHeuristics> loadPropertyData() {
        JavaRDD<Map<String, String>> csvDocuments = loadDocuments();
        return csvDocuments.flatMapToPair(new RecordToHeuristicsMap(fileName()));
    }

    /**
     * Retrieves a list of kind names (CSV file names) from the provider.
     *
     * @return a list of CSV file names as strings.
     * @throws RuntimeException if an error occurs while retrieving the file names.
     */
    @Override
    public List<String> getKindNames() {
        try {
            return provider.getCsvFileNames();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("Error getting csv file names", e);
        }
    }
}
