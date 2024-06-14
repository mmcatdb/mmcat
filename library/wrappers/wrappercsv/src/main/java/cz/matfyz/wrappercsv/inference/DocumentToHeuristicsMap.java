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
import java.util.stream.Collectors;

public class DocumentToHeuristicsMap implements PairFlatMapFunction<Map<String, String>, String, PropertyHeuristics> {

    private final String fileName;

    public DocumentToHeuristicsMap(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Iterator<Tuple2<String, PropertyHeuristics>> call(Map<String, String> record) {
        List<Tuple2<String, PropertyHeuristics>> result = record.entrySet().stream()
            .map(entry -> {
                String key = fileName + '/' + entry.getKey();
                PropertyHeuristics heuristics = buildHeuristics(key, entry.getValue(), 1, 1);
                return new Tuple2<>(key + "::" + entry.getValue(), heuristics);
            })
            .collect(Collectors.toList());

        return result.iterator();
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
