package cz.matfyz.querying.optimizer;

import java.util.HashMap;
import java.util.TreeMap;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Property;

// TODO: unused; you may remove this once youre sure there is nothing useful in here

public abstract class CostData {

    public abstract int estimateResultSize();

    public static class ScalarCostData extends CostData {
        // int cardinality;
        public int minValue, maxValue;

        // boolean isBool, isString; // always true
        // boolean isInt, isFloat;

        // for String:
        public int avgLength;

        // Later on, we may put type metadata here, but so far, it may be enough to infer the type from the operator

        @Override
        public int estimateResultSize() {
            return avgLength; // or some nominal value like 1 (would depend on datatype)
        }
    }

    public static class ObjectCostData extends CostData {
        public final HashMap<String, CostData> fields = new HashMap<>(); // the signatures matter when transforming

        @Override
        public int estimateResultSize() {
            int score = 0;

            // Add key lengths
            for (final var k : fields.keySet()) {
                score += k.length();
            }

            // Add expected value lengths
            for (final var v : fields.values()) {
                score += v.estimateResultSize();
            }

            return score;
        }
    }

    public static class ArrayCostData extends CostData {
        // int minSize, maxSize
        public int avgSize; // gathered from sampling perhaps
        public CostData elements;

        @Override
        public int estimateResultSize() {
            return avgSize * elements.estimateResultSize();
        }
    }

    public static class KindCostData {
        public ObjectCostData fields;
        public int count; // best estimate (or correct for non-virtual kind)

        public int estimateResultSize() {
            return count * fields.estimateResultSize();
        }
    }

    public record CacheEntry(int count, TreeMap<Property, CostData> fields) {}

    public static HashMap<String, CacheEntry> cacheByKind = new HashMap<>();



}
