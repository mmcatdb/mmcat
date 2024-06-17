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

public class RecordToHeuristicsMap implements PairFlatMapFunction<Document, String, PropertyHeuristics> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordToHeuristicsMap.class);

    private final String collectionName;

    public RecordToHeuristicsMap(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override
    public Iterator<Tuple2<String, PropertyHeuristics>> call(Document document) {
        ObjectArrayList<Tuple2<String, PropertyHeuristics>> result = new ObjectArrayList<Tuple2<String, PropertyHeuristics>>();

        appendHeuristics(collectionName, new Document(), 1, result, true);

        document.forEach((key, value) -> {
            appendHeuristics(collectionName + '/' + key, value, 1, result, true);
        });

        return result.iterator();
    }

    private void appendHeuristics(String key, Object value, int firstShare, ObjectArrayList<Tuple2<String, PropertyHeuristics>> result, boolean appendThisProperty) {
        if (value == null)
            return;
        if (appendThisProperty) {
            PropertyHeuristics heuristics = buildHeuristics(key, value, firstShare, 1);
            result.add(new Tuple2<String, PropertyHeuristics>(key + "::" + value.toString(), heuristics));
        }

        if (value instanceof Map) {
            appendMapHeuristics(key, ((Map<String, Object>) value).entrySet(), result);
        }
        else if (value instanceof List) {
            appendListHeuristics(key, (List<Object>) value, result);
        }
    }

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

    private void appendMapHeuristics(String parentName, Set<Map.Entry<String, Object>> nestedProperties,  ObjectArrayList<Tuple2<String, PropertyHeuristics>> result) {
        parentName += "/";

        for (Map.Entry<String, Object> value : nestedProperties) {
            String hierarchicalName = parentName + value.getKey();
            appendHeuristics(hierarchicalName, value.getValue(), 1, result, true);
        }
    }

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
