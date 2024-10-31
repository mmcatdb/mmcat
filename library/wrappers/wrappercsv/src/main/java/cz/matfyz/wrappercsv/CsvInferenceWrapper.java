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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @Override public AbstractInferenceWrapper copy() {
        return new CsvInferenceWrapper(this.provider, this.sparkSettings);
    }

    /**
     * Loads properties from the CSV data. This method is currently not implemented.
     *
     * @param loadSchema a boolean indicating whether to load the schema.
     * @param loadData a boolean indicating whether to load the data.
     * @return null as this method is not implemented.
     */
    @Override public JavaPairRDD<RawProperty, Share> loadProperties(boolean loadSchema, boolean loadData) {
        return null;
    }

    /**
     * Loads record schema descriptions (RSDs) from the CSV data.
     *
     * @return a {@link JavaRDD} of {@link RecordSchemaDescription} objects.
     */
    @Override public JavaRDD<RecordSchemaDescription> loadRSDs() {
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
        String[] header = null;

        try (InputStream inputStream = provider.getInputStream(kindName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String firstLine = reader.readLine();
            if (firstLine == null) {
                return context.emptyRDD();
            }

            char delimiter = detectDelimiter(firstLine);
            header = parseLine(firstLine, delimiter).toArray(new String[0]);

            String line;
            while ((line = reader.readLine()) != null) {
                List<String> elements = parseLine(line, delimiter);
                Map<String, String> lineMap = createLineMap(header, elements);
                lines.add(lineMap);
            }
        } catch (IOException e) {
            System.err.println("Error processing input stream: " + e.getMessage());
            return context.emptyRDD();
        }

        return context.parallelize(lines);
    }

    private char detectDelimiter(String line) {
        char[] possibleDelimiters = {',', '\t', ';', '.'};
        Map<Character, Integer> delimiterCount = new HashMap<>();
        for (char delimiter : possibleDelimiters) {
            int count = line.split(Pattern.quote(Character.toString(delimiter))).length - 1;
            delimiterCount.put(delimiter, count);
        }
        return Collections.max(delimiterCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private List<String> parseLine(String line, char delimiter) {
        String escapedDelimiter = Pattern.quote(Character.toString(delimiter));
        Pattern csvPattern = Pattern.compile("\"([^\"]*)\"|([^" + escapedDelimiter + "]+)");
        List<String> elements = new ArrayList<>();
        Matcher matcher = csvPattern.matcher(line);

        while (matcher.find()) {
            String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            elements.add(value);
        }
        return elements;
    }

    private Map<String, String> createLineMap(String[] header, List<String> elements) {
        Map<String, String> lineMap = new HashMap<>();
        for (int i = 0; i < elements.size(); i++) {
            if (i < header.length) {
                lineMap.put(header[i], elements.get(i));
            }
        }
        return lineMap;
    }

    /**
     * Loads pairs of strings and record schema descriptions (RSDs) from the CSV data.
     * This method is currently not implemented.
     *
     * @return null as this method is not implemented.
     */
    @Override public JavaPairRDD<String, RecordSchemaDescription> loadRSDPairs() {
        return null;
    }

    /**
     * Loads property schema pairs from the CSV data. This method is currently not implemented.
     *
     * @return nothing, as this method always throws an exception.
     * @throws UnsupportedOperationException always thrown as this method is not implemented.
     */
    @Override public JavaPairRDD<String, RecordSchemaDescription> loadPropertySchema() {
        throw new UnsupportedOperationException("Unimplemented method 'loadPropertySchema'");
    }

    /**
     * Loads property data from the CSV documents and maps them to {@link PropertyHeuristics}.
     *
     * @return a {@link JavaPairRDD} of string keys and {@link PropertyHeuristics} objects.
     */
    @Override public JavaPairRDD<String, PropertyHeuristics> loadPropertyData() {
        JavaRDD<Map<String, String>> csvDocuments = loadDocuments();
        return csvDocuments.flatMapToPair(new RecordToHeuristicsMap(fileName()));
    }

    /**
     * Retrieves a list of kind names (CSV file names) from the provider.
     *
     * @return a list of CSV file names as strings.
     * @throws RuntimeException if an error occurs while retrieving the file names.
     */
    @Override public List<String> getKindNames() {
        try {
            return provider.getCsvFileNames();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("Error getting csv file names", e);
        }
    }
}
