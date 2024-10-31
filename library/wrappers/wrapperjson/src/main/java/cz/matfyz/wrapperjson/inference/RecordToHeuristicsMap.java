package cz.matfyz.wrapperjson.inference;

import cz.matfyz.core.rsd.utils.BlobClobHashing;
import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.utils.BasicHashFunction;
import cz.matfyz.core.rsd.utils.BloomFilter;
import java.sql.Blob;
import java.sql.Clob;
import java.util.HashSet;
import java.util.Iterator;
import org.bson.Document;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shaded.parquet.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import scala.Tuple2;

/**
 * A function that maps a BSON {@link Document} to a list of Tuples, where each Tuple contains a string key
 * and a {@link PropertyHeuristics} object. This class is used to perform heuristic analysis on the properties
 * of JSON documents.
 */
public class RecordToHeuristicsMap implements PairFlatMapFunction<Document, String, PropertyHeuristics> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordToHeuristicsMap.class);

    private final String collectionName;

    /**
     * Constructs a new {@code RecordToHeuristicsMap} with the specified collection name.
     *
     * @param collectionName the name of the collection being processed, used as a prefix for the keys.
     */
    public RecordToHeuristicsMap(String collectionName) {
        this.collectionName = collectionName;
    }

    /**
     * Applies this function to the given BSON {@link Document} and returns an iterator over the
     * generated tuples.
     *
     * @param document a BSON {@link Document} representing a JSON document.
     * @return an iterator over tuples containing keys and their corresponding {@link PropertyHeuristics}.
     */
    @Override public Iterator<Tuple2<String, PropertyHeuristics>> call(Document document) {
        ObjectArrayList<Tuple2<String, PropertyHeuristics>> result = new ObjectArrayList<>();

        appendHeuristics(collectionName, new Document(), 1, result, true);

        document.forEach((key, value) -> {
            appendHeuristics(collectionName + '/' + key, value, 1, result, true);
        });

        return result.iterator();
    }

    /**
     * Appends heuristics for a given key-value pair to the result list.
     *
     * @param key the hierarchical key for the current property.
     * @param value the value of the property to analyze.
     * @param firstShare the initial share value for the property.
     * @param result the list to append the heuristics result to.
     * @param appendThisProperty whether to append the current property to the result.
     */
    private void appendHeuristics(String key, Object value, int firstShare, ObjectArrayList<Tuple2<String, PropertyHeuristics>> result, boolean appendThisProperty) {
        if (value == null)
            return;
        if (appendThisProperty) {
            PropertyHeuristics heuristics = buildHeuristics(key, value, firstShare, 1);
            result.add(new Tuple2<>(key + "::" + value.toString(), heuristics));
        }

        if (value instanceof Map) {
            appendMapHeuristics(key, ((Map<String, Object>) value).entrySet(), result);
        }
        else if (value instanceof List) {
            appendListHeuristics(key, (List<Object>) value, result);
        }
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
                }
                else if (value instanceof Blob) {
                    valueToSave = BlobClobHashing.blobToHash((Blob) value);
                }
                else if (value instanceof Clob) {
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

    /**
     * Appends heuristics for properties within a map to the result list.
     *
     * @param parentName the parent hierarchical name.
     * @param nestedProperties the set of entries representing nested properties.
     * @param result the list to append the heuristics result to.
     */
    private void appendMapHeuristics(String parentName, Set<Map.Entry<String, Object>> nestedProperties,  ObjectArrayList<Tuple2<String, PropertyHeuristics>> result) {
        parentName += "/";

        for (Map.Entry<String, Object> value : nestedProperties) {
            String hierarchicalName = parentName + value.getKey();
            appendHeuristics(hierarchicalName, value.getValue(), 1, result, true);
        }
    }

    /**
     * Appends heuristics for elements within a list to the result list.
     *
     * @param parentName the parent hierarchical name.
     * @param elements the list of elements to process.
     * @param result the list to append the heuristics result to.
     */
    private void appendListHeuristics(String parentName, List<Object> elements,  ObjectArrayList<Tuple2<String, PropertyHeuristics>> result) {
        Set<Object> visited = new HashSet<>();
        String hierarchicalName = parentName + "/_";

        for (Object value : elements) {
            if (visited.stream().anyMatch(v -> value.getClass().isInstance(v))) {
                appendHeuristics(hierarchicalName, value, 0, result, false);
            } else {
                visited.add(value);
                appendHeuristics(hierarchicalName, value, 1, result, true);
            }
        }

    }
}
