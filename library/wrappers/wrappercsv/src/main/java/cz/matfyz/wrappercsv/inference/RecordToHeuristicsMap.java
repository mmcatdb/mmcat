package cz.matfyz.wrappercsv.inference;

import cz.matfyz.core.rsd.utils.BlobClobHashing;
import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.utils.BasicHashFunction;
import cz.matfyz.core.rsd.utils.BloomFilter;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import scala.Tuple2;

import java.sql.Blob;
import java.sql.Clob;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A function that maps a CSV record represented as a Map of key-value pairs
 * to a list of Tuples, where each Tuple contains a string key and a {@link PropertyHeuristics} object.
 * This class is used to perform heuristic analysis on the properties of CSV records.
 */
public class RecordToHeuristicsMap implements PairFlatMapFunction<Map<String, String>, String, PropertyHeuristics> {

    private final String fileName;

    /**
     * Constructs a new instance of {@code RecordToHeuristicsMap}.
     *
     * @param fileName the name of the file being processed, used as a prefix for the keys.
     */
    public RecordToHeuristicsMap(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Applies this function to the given CSV record and returns an iterator over the
     * generated tuples.
     *
     * @param record a Map representing a CSV record, where each entry is a key-value pair.
     * @return an iterator over tuples containing keys and their corresponding {@link PropertyHeuristics}.
     */
    @Override
    public Iterator<Tuple2<String, PropertyHeuristics>> call(Map<String, String> record) {
        List<Tuple2<String, PropertyHeuristics>> result = record.entrySet().stream()
            .map(entry -> {
                String key = fileName + '/' + entry.getKey();
                PropertyHeuristics heuristics = buildHeuristics(key, entry.getValue(), 1, 1);
                return new Tuple2<>(key + "::" + entry.getValue(), heuristics);
            })
            .toList();

        return result.iterator();
    }

    /**
     * Builds a {@link PropertyHeuristics} object for a given key-value pair.
     * This method is responsible for setting various properties based on the value type.
     *
     * @param key the hierarchical name to be used for the property.
     * @param value the value to be analyzed and stored in the heuristics.
     * @param first the first occurrence count.
     * @param total the total occurrence count.
     * @return a new instance of {@link PropertyHeuristics} with computed properties.
     */
    private PropertyHeuristics buildHeuristics(String key, Object value, int first, int total) {
        return new PropertyHeuristics() {
            {
                setHierarchicalName(key);
                Object valueToSave = value;
                if (value instanceof Number) {
                    setTemp(((Number) value).doubleValue());
                } else if (value instanceof Comparable) {
                    double resultOfHashFunction = new BasicHashFunction().apply(value).doubleValue();
                    setTemp(resultOfHashFunction);
                } else if (value instanceof Blob) {
                    valueToSave = BlobClobHashing.blobToHash((Blob) value);
                } else if (value instanceof Clob) {
                    valueToSave = BlobClobHashing.clobToHash((Clob) value);
                }
                setMin(valueToSave);
                setMax(valueToSave);
                setFirst(first);
                setCount(total);
                setUnique(total == 1);
                BloomFilter bloomFilter = new BloomFilter();
                if (value != null) {
                    bloomFilter.add(valueToSave);
                }
                setBloomFilter(bloomFilter);
                addToStartingEnding(valueToSave);
            }
        };
    }
}
