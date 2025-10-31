package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.wrappercsv.inference.RecordToHeuristicsMap;
import cz.matfyz.wrappercsv.inference.MapCsvDocument;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
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
     */
    public CsvInferenceWrapper(CsvProvider provider, String kindName, SparkSettings sparkSettings) {
        super(kindName, sparkSettings);
        this.provider = provider;
    }

    /**
     * Loads record schema descriptions (RSDs) from the CSV data.
     */
    @Override public JavaRDD<RecordSchemaDescription> loadRSDs() {
        return loadDocuments().map(MapCsvDocument::process);
    }

    /**
     * Loads property schema pairs from the CSV data.
     * This method is currently not implemented.
     */
    @Override public JavaPairRDD<String, RecordSchemaDescription> loadPropertySchema() {
        throw new UnsupportedOperationException("Unimplemented method 'loadPropertySchema'");
    }

    /**
     * Loads property data from the CSV documents and maps them to {@link PropertyHeuristics}.
     */
    @Override public JavaRDD<PropertyHeuristics> loadPropertyData() {
        return loadDocuments().flatMap(new RecordToHeuristicsMap(kindName));
    }

    /**
     * Loads documents from the CSV file and parses them into a list of maps,
     * where each map represents a CSV row with header names as keys.
     */
    public JavaRDD<Map<String, String>> loadDocuments() {
        final var lines = new ArrayList<Map<String, String>>();

        final CsvSchema baseSchema = CsvSchema.emptySchema()
            .withColumnSeparator(provider.settings.separator())
            .withEscapeChar('\\');

        try (
            InputStream inputStream = provider.getInputStream();
        ) {
            if (!provider.settings.hasHeader()) {
                // If there is no header, we have to read the first line to get the number of columns and create a default header.
                final MappingIterator<String[]> headerReader = new CsvMapper()
                    .readerFor(String[].class)
                    .with(CsvParser.Feature.WRAP_AS_ARRAY)
                    .with(baseSchema)
                    .readValues(inputStream);

                if (!headerReader.hasNext())
                    return getContext().emptyRDD();

                final String[] firstLine = headerReader.next();
                final String[] header = new String[firstLine.length];
                for (int i = 0; i < firstLine.length; i++)
                    header[i] = "" + i;

                // We have to read the lines as String[] and manually convert them to Map<String, String>, because the stream already started (we had to create the header).
                // We can't "switch" to a different reader, because the stream is already consumed.
                lines.add(createLineMap(header, firstLine));

                while (headerReader.hasNext())
                    lines.add(createLineMap(header, headerReader.next()));
            }
            else {
                final MappingIterator<Map<String, String>> reader = new CsvMapper()
                    .readerFor(Map.class)
                    .with(baseSchema.withHeader())
                    .readValues(inputStream);

                while (reader.hasNext())
                    lines.add(reader.next());
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return getContext().parallelize(lines);
    }

    private Map<String, String> createLineMap(String[] header, String[] elements) {
        final Map<String, String> lineMap = new HashMap<>();
        final int columns = Math.min(header.length, elements.length);
        for (int i = 0; i < columns; i++)
            lineMap.put(header[i], elements[i]);

        return lineMap;
    }

}
