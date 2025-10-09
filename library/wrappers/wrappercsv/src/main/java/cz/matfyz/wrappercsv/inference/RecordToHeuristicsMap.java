package cz.matfyz.wrappercsv.inference;

import cz.matfyz.core.rsd.PropertyHeuristics;
import org.apache.spark.api.java.function.FlatMapFunction;

import java.util.Iterator;
import java.util.Map;

/**
 * A function that maps a CSV record represented as a Map of key-value pairs
 * to a list of Tuples, where each Tuple contains a string key and a {@link PropertyHeuristics} object.
 * This class is used to perform heuristic analysis on the properties of CSV records.
 */
public class RecordToHeuristicsMap implements FlatMapFunction<Map<String, String>, PropertyHeuristics> {

    private final String filename;

    /**
     * Constructs a new instance of {@code RecordToHeuristicsMap}.
     *
     * @param filename the name of the file being processed, used as a prefix for the keys.
     */
    public RecordToHeuristicsMap(String filename) {
        this.filename = filename;
    }

    /**
     * Applies this function to the given CSV record and returns an iterator over the
     * generated tuples.
     *
     * @param record a Map representing a CSV record, where each entry is a key-value pair.
     * @return an iterator over tuples containing keys and their corresponding {@link PropertyHeuristics}.
     */
    @Override public Iterator<PropertyHeuristics> call(Map<String, String> record) {
        return record.entrySet().stream()
            .map(entry -> {
                final String key = filename + '/' + entry.getKey();
                final var value = entry.getValue();
                return PropertyHeuristics.createForKeyValuePair(key, value);
            })
            .iterator();
    }

}
