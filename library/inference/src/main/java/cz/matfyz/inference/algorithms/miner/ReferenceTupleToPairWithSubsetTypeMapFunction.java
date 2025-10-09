package cz.matfyz.inference.algorithms.miner;

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

    @Override public Tuple2<Tuple2<PropertyHeuristics, PropertyHeuristics>, SubsetType> call(Tuple2<PropertyHeuristics, PropertyHeuristics> tuple) throws Exception {
        final SubsetType type = formsSubset(tuple._1, tuple._2);
        return new Tuple2<>(tuple, type);
    }

    private static SubsetType formsSubset(PropertyHeuristics f1, PropertyHeuristics f2) {
        if (f1.hierarchicalName.equals(f2.hierarchicalName))
            return SubsetType.EMPTY; // a je vyfiltrovaný v dalším kroce

        final int minCompare = compareValues(f1.getMin(), f2.getMin());
        final int maxCompare = compareValues(f1.getMax(), f2.getMax());
        final int avgCompare = compareValues(f1.getAverage(), f2.getAverage());

        final SubsetType minType = minCompare == 0
            ? SubsetType.FULL
            : minCompare > 0
                ? SubsetType.PARTIAL
                : SubsetType.EMPTY;

        final SubsetType maxType = maxCompare == 0
            ? SubsetType.FULL
            : maxCompare < 0
                ? SubsetType.PARTIAL
                : SubsetType.EMPTY;

        final SubsetType avgType = avgCompare == 0
            ? SubsetType.FULL
            : SubsetType.EMPTY;

        final List<SubsetType> types = new ArrayList<>(Arrays.asList(minType, maxType, avgType));

        if (Collections.min(types, Comparator.comparingInt(Enum::ordinal)) == SubsetType.EMPTY)
            return SubsetType.EMPTY;

        final int bfCompare = f1.getBloomFilter().isSubsetOf(f2.getBloomFilter());

        final SubsetType bfType = bfCompare == 0
            ? SubsetType.FULL
            : bfCompare == 1
                ? SubsetType.PARTIAL
                : SubsetType.EMPTY;

        types.add(bfType);

        if (Collections.min(types, Comparator.comparingInt(Enum::ordinal)) == SubsetType.FULL)
            return SubsetType.FULL;

        types.remove(avgType);

        return Collections.min(types, Comparator.comparingInt(Enum::ordinal)) == SubsetType.EMPTY
            ? SubsetType.EMPTY
            : SubsetType.PARTIAL;
    }

    private static Integer compareValues(Object a, Object b) {
        if (a == null || b == null)
            return null;
        if (a instanceof Number aNumber && b instanceof Number bNumber)
            return Double.compare(aNumber.doubleValue(), bNumber.doubleValue());
        if (a instanceof Comparable aComparable && b instanceof Comparable && a.getClass().equals(b.getClass()))
            return aComparable.compareTo(b);

        return a.toString().compareTo(b.toString());
    }
}
