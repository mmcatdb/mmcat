package cz.matfyz.inference.algorithms.miner.functions;

import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.SubsetType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

public class ReferenceTupleToPairWithSubsetTypeMapFunction implements PairFunction<Tuple2<PropertyHeuristics, PropertyHeuristics>, Tuple2<PropertyHeuristics, PropertyHeuristics>, SubsetType> {
    @Override
    public Tuple2<Tuple2<PropertyHeuristics, PropertyHeuristics>, SubsetType> call(Tuple2<PropertyHeuristics, PropertyHeuristics> tuple) throws Exception {
        SubsetType type = formsSubset(tuple._1, tuple._2);
        return new Tuple2<>(tuple, type);
    }

    private SubsetType formsSubset(PropertyHeuristics f1, PropertyHeuristics f2) {
                if (f1.hierarchicalName.equals(f2.hierarchicalName)) return SubsetType.EMPTY; // a je vyfiltrovaný v dalším kroce
        int minCompare = compareValues(f1.getMin(), f2.getMin());
        int maxCompare = compareValues(f1.getMax(), f2.getMax());
        int avgCompare = compareValues(f1.getAverage(), f2.getAverage());
        SubsetType minType = minCompare == 0 ? SubsetType.FULL : minCompare > 0
                ? SubsetType.PARTIAL : SubsetType.EMPTY;
        SubsetType maxType = maxCompare == 0 ? SubsetType.FULL : maxCompare < 0
                ? SubsetType.PARTIAL : SubsetType.EMPTY;
        SubsetType avgType = avgCompare == 0 ? SubsetType.FULL : SubsetType.EMPTY;

        List<SubsetType> types = new ArrayList<>(Arrays.asList(minType, maxType, avgType));

        if (Collections.min(types, Comparator.comparingInt(Enum::ordinal)) == SubsetType.EMPTY) {
            return SubsetType.EMPTY;
        }

        int bfCompare = f1.getBloomFilter().isSubsetOf(f2.getBloomFilter());

        SubsetType bfType = bfCompare == 0 ? SubsetType.FULL : bfCompare == 1
                ? SubsetType.PARTIAL : SubsetType.EMPTY;

        types.add(bfType);

        if (Collections.min(types, Comparator.comparingInt(Enum::ordinal)) == SubsetType.FULL) {
            return SubsetType.FULL;
        } else {
            types.remove(avgType);
            if (Collections.min(types, Comparator.comparingInt(Enum::ordinal)) == SubsetType.EMPTY) {
                return SubsetType.EMPTY;
            } else {
                return SubsetType.PARTIAL;
            }
        }
    }

    public Integer compareValues(Object val, Object other) {
        if (val == null || other == null) {
            return null;
        }
        if (val instanceof Number && other instanceof Number) {
            return Double.compare(((Number) val).doubleValue(), ((Number) other).doubleValue());
        }
        if (val instanceof Comparable && other instanceof Comparable && val.getClass().equals(other.getClass())) {
            return ((Comparable) val).compareTo(other);
        }
        return val.toString().compareTo(other.toString());
    }
}
